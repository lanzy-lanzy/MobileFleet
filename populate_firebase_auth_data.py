#!/usr/bin/env python3
"""
Populate Firebase with sample data including Firebase Authentication users
This script creates Firebase Auth users and links them to driver records in Firestore
"""

import os
import sys
import django
from datetime import datetime, timedelta
import random

# Setup Django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'MobileFleet.settings')
django.setup()

from monitoring.firebase_service import firebase_service
from monitoring.utils import generate_qr_code, upload_to_cloudinary
import argparse

def clear_firebase_data():
    """Clear existing Firebase data"""
    print("🧹 Clearing existing Firebase data...")

    try:
        # Clear terminals
        terminals = firebase_service.get_all_terminals()
        for terminal in terminals:
            firebase_service.delete_terminal(terminal['id'])
        print(f"   ✅ Cleared {len(terminals)} terminals")

        # Clear drivers
        drivers = firebase_service.get_all_drivers()
        for driver in drivers:
            firebase_service.delete_driver(driver['id'])
        print(f"   ✅ Cleared {len(drivers)} drivers")

        # Clear trips
        trips = firebase_service.get_all_trips()
        for trip in trips:
            firebase_service.delete_trip(trip['id'])
        print(f"   ✅ Cleared {len(trips)} trips")

        # Clear commission settings
        try:
            docs = firebase_service.db.collection('commission_settings').stream()
            for doc in docs:
                doc.reference.delete()
            print("   ✅ Cleared commission settings")
        except Exception as e:
            print(f"   ⚠️  Error clearing commission settings: {e}")

        print("✅ Firebase data cleared successfully!")
        return True

    except Exception as e:
        print(f"❌ Error clearing Firebase data: {e}")
        return False

def populate_firebase_auth_data(with_qr=True, clear_first=False):
    """Populate Firebase with sample data including Auth users"""
    print("🚀 Populating Firebase with Authentication-enabled sample data...")
    if with_qr:
        print("📱 Including QR code generation for terminals")
    print("=" * 60)

    try:
        # Clear existing data if requested
        if clear_first:
            if not clear_firebase_data():
                print("❌ Failed to clear existing data. Aborting.")
                return False
            print()  # Add spacing
        # Sample terminals data
        terminals_data = [
            {'name': 'Dumingag Terminal', 'latitude': 8.1234, 'longitude': 123.5678, 'is_active': True},
            {'name': 'Molave Terminal', 'latitude': 8.0987, 'longitude': 123.4567, 'is_active': True},
            {'name': 'Pagadian Terminal', 'latitude': 7.8456, 'longitude': 123.4321, 'is_active': True},
            {'name': 'Ozamiz Terminal', 'latitude': 8.1567, 'longitude': 123.8901, 'is_active': True},
            {'name': 'Dipolog Terminal', 'latitude': 8.5890, 'longitude': 123.3456, 'is_active': True},
        ]

        # Sample drivers data with Firebase Auth integration
        drivers_auth_data = [
            {
                'name': 'Juan Dela Cruz',
                'email': 'juan.delacruz@mobilefleet.com',
                'password': 'password123',
                'contact': '+63 912 345 6789',
                'license_number': 'N01-12-345678',
                'is_active': True,
                'address': 'Dumingag, Zamboanga del Sur',
                'emergency_contact': 'Rosa Dela Cruz',
                'emergency_phone': '+63 912 345 6790',
                'status': 'active',
                'salary_type': 'commission',
                'base_salary': 0.0,
                'commission_rate': 0.05,
                'commission_per_passenger': 2.0,
                'trip_completion_bonus': 5.0
            },
            {
                'name': 'Maria Santos',
                'email': 'maria.santos@mobilefleet.com',
                'password': 'password123',
                'contact': '+63 923 456 7890',
                'license_number': 'N02-13-456789',
                'is_active': True,
                'address': 'Molave, Zamboanga del Sur',
                'emergency_contact': 'Jose Santos',
                'emergency_phone': '+63 923 456 7891',
                'status': 'active',
                'salary_type': 'commission',
                'base_salary': 0.0,
                'commission_rate': 0.06,
                'commission_per_passenger': 2.5,
                'trip_completion_bonus': 6.0
            },
            {
                'name': 'Pedro Gonzales',
                'email': 'pedro.gonzales@mobilefleet.com',
                'password': 'password123',
                'contact': '+63 934 567 8901',
                'license_number': 'N03-14-567890',
                'is_active': True,
                'address': 'Pagadian City, Zamboanga del Sur',
                'emergency_contact': 'Carmen Gonzales',
                'emergency_phone': '+63 934 567 8902',
                'status': 'active',
                'salary_type': 'hybrid',
                'base_salary': 1000.0,
                'commission_rate': 0.03,
                'commission_per_passenger': 1.5,
                'trip_completion_bonus': 4.0
            },
            {
                'name': 'Ana Rodriguez',
                'email': 'ana.rodriguez@mobilefleet.com',
                'password': 'password123',
                'contact': '+63 945 678 9012',
                'license_number': 'N04-15-678901',
                'is_active': True,
                'address': 'Ozamiz City, Misamis Occidental',
                'emergency_contact': 'Miguel Rodriguez',
                'emergency_phone': '+63 945 678 9013',
                'status': 'active',
                'salary_type': 'commission',
                'base_salary': 0.0,
                'commission_rate': 0.055,
                'commission_per_passenger': 2.2,
                'trip_completion_bonus': 5.5
            },
            {
                'name': 'Carlos Mendoza',
                'email': 'carlos.mendoza@mobilefleet.com',
                'password': 'password123',
                'contact': '+63 956 789 0123',
                'license_number': 'N05-16-789012',
                'is_active': False,
                'address': 'Dipolog City, Zamboanga del Norte',
                'emergency_contact': 'Elena Mendoza',
                'emergency_phone': '+63 956 789 0124',
                'status': 'inactive',
                'salary_type': 'commission',
                'base_salary': 0.0,
                'commission_rate': 0.05,
                'commission_per_passenger': 2.0,
                'trip_completion_bonus': 5.0
            },
        ]

        # Create terminals with QR codes
        print("1. Creating terminals with QR codes...")
        terminal_ids = []
        for terminal_data in terminals_data:
            try:
                print(f"   📱 Creating terminal: {terminal_data['name']}...")

                # Create terminal first to get ID
                terminal_id = firebase_service.create_terminal(terminal_data)
                if terminal_id:
                    terminal_ids.append(terminal_id)

                    # Generate QR code if requested
                    if with_qr:
                        qr_data = f"terminal_id:{terminal_id}"
                        qr_image = generate_qr_code(qr_data)

                        # Upload to Cloudinary
                        upload_result = upload_to_cloudinary(qr_image, f"terminal_{terminal_id}")

                        # Update terminal with QR code data and URL
                        update_data = {'qr_code': qr_data}

                        if upload_result:
                            qr_code_url = upload_result.get('secure_url', upload_result.get('url'))
                            update_data['qr_code_url'] = qr_code_url
                            print(f"   ✅ QR code uploaded: {qr_code_url}")
                        else:
                            print(f"   ⚠️  QR code upload failed, but QR data stored")

                        # Update terminal with QR data
                        success = firebase_service.update_terminal(terminal_id, update_data)
                        if success:
                            print(f"   ✅ Terminal created with QR: {terminal_data['name']}")
                            print(f"      QR Data: {qr_data}")
                        else:
                            print(f"   ❌ Failed to update terminal with QR data")
                    else:
                        print(f"   ✅ Terminal created: {terminal_data['name']} (no QR code)")
                else:
                    print(f"   ❌ Failed to create terminal: {terminal_data['name']}")

            except Exception as e:
                print(f"   ❌ Error creating terminal {terminal_data['name']}: {e}")

        # Create Firebase Auth users and driver records
        print("\n2. Creating Firebase Auth users and drivers...")
        driver_ids = []
        auth_users = []
        
        for driver_data in drivers_auth_data:
            try:
                # Extract auth data
                email = driver_data['email']
                password = driver_data['password']
                name = driver_data['name']
                
                # Create Firebase Auth user
                print(f"   Creating Auth user for {name}...")
                auth_uid = firebase_service.create_auth_user(email, password, name)
                
                if auth_uid:
                    print(f"   ✅ Created Firebase Auth user: {email}")
                    
                    # Prepare driver data (remove password, add auth_uid)
                    driver_record = driver_data.copy()
                    driver_record.pop('password')  # Remove password from Firestore data
                    driver_record['auth_uid'] = auth_uid
                    
                    # Create driver record in Firestore
                    driver_id = firebase_service.create_driver(driver_record)
                    
                    if driver_id:
                        driver_ids.append(driver_id)
                        auth_users.append({
                            'auth_uid': auth_uid,
                            'driver_id': driver_id,
                            'email': email,
                            'name': name
                        })
                        print(f"   ✅ Created driver record: {name} (ID: {driver_id})")
                    else:
                        print(f"   ❌ Failed to create driver record for {name}")
                else:
                    print(f"   ❌ Failed to create Auth user for {name}")
                    
            except Exception as e:
                print(f"   ❌ Error creating user {driver_data['name']}: {e}")

        # Create commission settings
        print("\n3. Creating commission settings...")
        commission_settings = {
            'base_commission_rate': 0.05,
            'passenger_bonus': 2.0,
            'trip_completion_bonus': 5.0,
            'monthly_trip_bonus_threshold': 100,
            'monthly_trip_bonus_amount': 200.0,
            'peak_hour_multiplier': 1.5,
            'peak_hours': ['07:00-09:00', '17:00-19:00'],
            'weekend_multiplier': 1.2,
            'updated_at': datetime.now()
        }

        try:
            doc_ref = firebase_service.db.collection('commission_settings').document()
            doc_ref.set(commission_settings)
            print("✅ Created commission settings")
        except Exception as e:
            print(f"⚠️  Failed to create commission settings: {e}")

        # Create sample trips
        print("\n4. Creating sample trips...")
        trip_statuses = ['in_progress', 'completed', 'cancelled']

        for i in range(15):
            if driver_ids:  # Only create trips if we have drivers
                trip_data = {
                    'driver_id': random.choice(driver_ids),
                    'start_terminal': random.choice(terminal_ids),
                    'destination_terminal': random.choice(terminal_ids),
                    'passengers': random.randint(5, 25),
                    'status': random.choice(trip_statuses),
                }

                # Add timestamps for completed trips
                if trip_data['status'] == 'completed':
                    start_time = datetime.now() - timedelta(hours=random.randint(1, 72))
                    trip_data['start_time'] = start_time
                    trip_data['arrival_time'] = start_time + timedelta(minutes=random.randint(30, 180))
                elif trip_data['status'] == 'in_progress':
                    trip_data['start_time'] = datetime.now() - timedelta(minutes=random.randint(10, 120))

                trip_id = firebase_service.create_trip(trip_data)
                if trip_id:
                    print(f"✅ Created trip: {trip_data['status']} - {trip_data['passengers']} passengers")

        print("\n" + "=" * 60)
        print("🎉 Firebase Authentication data populated successfully!")
        print(f"📊 Created {len(terminal_ids)} terminals, {len(driver_ids)} drivers with Auth, and 15 trips")
        
        print("\n🔐 Firebase Authentication Users Created:")
        for user in auth_users:
            print(f"   📱 {user['name']}: {user['email']} / password123")
            print(f"      Auth UID: {user['auth_uid']}")
            print(f"      Driver ID: {user['driver_id']}")
        
        print(f"\n🌐 Visit http://localhost:8000/ to see the data in action!")
        print("📱 Use Firebase Auth for mobile app authentication!")

    except Exception as e:
        print(f"❌ Error: {e}")
        return False

    return True

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Populate Firebase with sample data including Authentication')
    parser.add_argument('--no-qr', action='store_true', help='Skip QR code generation')
    parser.add_argument('--clear', action='store_true', help='Clear existing data before populating')

    args = parser.parse_args()

    with_qr = not args.no_qr
    clear_first = args.clear

    success = populate_firebase_auth_data(with_qr=with_qr, clear_first=clear_first)
    if not success:
        sys.exit(1)
