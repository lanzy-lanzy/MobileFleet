from django.core.management.base import BaseCommand
from monitoring.firebase_service import firebase_service
from monitoring.utils import generate_qr_code, upload_to_cloudinary
from datetime import datetime, timedelta
import random

class Command(BaseCommand):
    help = 'Populate Firebase with sample data including QR codes'

    def add_arguments(self, parser):
        parser.add_argument(
            '--clear',
            action='store_true',
            help='Clear existing data before populating',
        )
        parser.add_argument(
            '--with-qr',
            action='store_true',
            help='Generate QR codes for terminals (requires Cloudinary credentials)',
        )

    def handle(self, *args, **options):
        self.stdout.write("🚀 Populating Firebase with sample data...")
        if options['with_qr']:
            self.stdout.write("📱 QR codes will be generated and uploaded to Cloudinary")
        self.stdout.write("=" * 50)

        try:
            # Sample terminals data
            terminals_data = [
                {'name': 'Dumingag Terminal', 'latitude': 8.1234, 'longitude': 123.5678, 'is_active': True},
                {'name': 'Molave Terminal', 'latitude': 8.0987, 'longitude': 123.4567, 'is_active': True},
                {'name': 'Pagadian Terminal', 'latitude': 7.8456, 'longitude': 123.4321, 'is_active': True},
                {'name': 'Ozamiz Terminal', 'latitude': 8.1567, 'longitude': 123.8901, 'is_active': True},
                {'name': 'Dipolog Terminal', 'latitude': 8.5890, 'longitude': 123.3456, 'is_active': True},
            ]

            # Sample drivers data
            drivers_data = [
                {'name': 'Juan Dela Cruz', 'contact': '+63 912 345 6789', 'license_number': 'N01-12-345678', 'is_active': True},
                {'name': 'Maria Santos', 'contact': '+63 923 456 7890', 'license_number': 'N02-13-456789', 'is_active': True},
                {'name': 'Pedro Gonzales', 'contact': '+63 934 567 8901', 'license_number': 'N03-14-567890', 'is_active': True},
                {'name': 'Ana Rodriguez', 'contact': '+63 945 678 9012', 'license_number': 'N04-15-678901', 'is_active': True},
                {'name': 'Carlos Mendoza', 'contact': '+63 956 789 0123', 'license_number': 'N05-16-789012', 'is_active': False},
            ]

            # Create terminals
            self.stdout.write("1. Creating terminals...")
            terminal_ids = []
            for terminal_data in terminals_data:
                # Generate QR code if requested (before creating terminal)
                if options['with_qr']:
                    try:
                        self.stdout.write(f"   📱 Generating QR code for {terminal_data['name']}...")
                        # Create terminal first to get ID
                        terminal_id = firebase_service.create_terminal(terminal_data)
                        if terminal_id:
                            terminal_ids.append(terminal_id)

                            # Use consistent QR data format
                            qr_data = f"terminal_id:{terminal_id}"
                            qr_image = generate_qr_code(qr_data)
                            upload_result = upload_to_cloudinary(qr_image, f"terminal_{terminal_id}")

                            # Update terminal with both QR code data and URL
                            update_data = {'qr_code': qr_data}  # Store the actual QR code data

                            if upload_result:
                                # Extract the secure URL from the upload result
                                qr_code_url = upload_result.get('secure_url', upload_result.get('url'))
                                update_data['qr_code_url'] = qr_code_url
                                self.stdout.write(f"   ✅ QR code uploaded: {qr_code_url}")
                            else:
                                self.stdout.write(self.style.WARNING(f"   ⚠️  QR code upload failed, but QR data stored"))

                            # Try to update the terminal with QR data
                            success = firebase_service.update_terminal(terminal_id, update_data)
                            if success:
                                self.stdout.write(f"   ✅ QR code data stored: {qr_data}")
                            else:
                                self.stdout.write(self.style.ERROR(f"   ❌ Failed to store QR code data"))

                            self.stdout.write(self.style.SUCCESS(f"✅ Created terminal: {terminal_data['name']}"))
                        else:
                            self.stdout.write(self.style.ERROR(f"❌ Failed to create terminal: {terminal_data['name']}"))
                    except Exception as e:
                        self.stdout.write(self.style.WARNING(f"   ⚠️  QR code generation failed: {e}"))
                else:
                    # Create terminal without QR code
                    terminal_id = firebase_service.create_terminal(terminal_data)
                    if terminal_id:
                        terminal_ids.append(terminal_id)
                        self.stdout.write(self.style.SUCCESS(f"✅ Created terminal: {terminal_data['name']}"))

            # Create drivers
            self.stdout.write("\n2. Creating drivers...")
            driver_ids = []
            for driver_data in drivers_data:
                driver_id = firebase_service.create_driver(driver_data)
                if driver_id:
                    driver_ids.append(driver_id)
                    self.stdout.write(self.style.SUCCESS(f"✅ Created driver: {driver_data['name']}"))

            # Create sample trips
            self.stdout.write("\n3. Creating sample trips...")
            trip_statuses = ['in_progress', 'completed', 'cancelled']

            for i in range(10):
                trip_data = {
                    'driver_id': random.choice(driver_ids),
                    'start_terminal': random.choice(terminal_ids),
                    'destination_terminal': random.choice(terminal_ids),
                    'passengers': random.randint(5, 25),
                    'status': random.choice(trip_statuses),
                }

                # Add timestamps for completed trips
                if trip_data['status'] == 'completed':
                    start_time = datetime.now() - timedelta(hours=random.randint(1, 24))
                    trip_data['start_time'] = start_time
                    trip_data['arrival_time'] = start_time + timedelta(minutes=random.randint(30, 180))
                elif trip_data['status'] == 'in_progress':
                    trip_data['start_time'] = datetime.now() - timedelta(minutes=random.randint(10, 120))

                trip_id = firebase_service.create_trip(trip_data)
                if trip_id:
                    self.stdout.write(self.style.SUCCESS(f"✅ Created trip: {trip_data['status']} - {trip_data['passengers']} passengers"))

            self.stdout.write("\n" + "=" * 50)
            self.stdout.write(self.style.SUCCESS("🎉 Sample data populated successfully!"))
            self.stdout.write(f"📊 Created {len(terminal_ids)} terminals, {len(driver_ids)} drivers, and 10 trips")
            self.stdout.write("\n🌐 Visit http://localhost:8000/ to see the data in action!")

        except Exception as e:
            self.stdout.write(self.style.ERROR(f"❌ Error: {e}"))