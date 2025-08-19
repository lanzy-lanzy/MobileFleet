#!/usr/bin/env python3
"""
Script to fix existing terminals by adding missing qr_code field
Run this script from your Django project directory where manage.py is located
"""

import os
import sys
import django
from datetime import datetime

# Add the Django project to the Python path
# You may need to adjust this path to point to your Django project directory
django_project_path = "."  # Adjust this if needed
sys.path.append(django_project_path)

# Set up Django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'your_project.settings')  # Adjust this
django.setup()

# Now import Django modules
from monitoring.firebase_service import firebase_service

def fix_qr_codes():
    print("🔧 Fixing terminals by adding missing qr_code fields...")
    print("=" * 50)

    try:
        # Get all terminals
        terminals = firebase_service.get_all_terminals()
        print(f"Found {len(terminals)} terminals")

        for terminal in terminals:
            terminal_id = terminal.get('id') or terminal.get('terminal_id')
            name = terminal.get('name', 'Unknown')
            qr_code = terminal.get('qr_code')

            print(f"\nProcessing: {name} (ID: {terminal_id})")
            print(f"  Current qr_code field: {qr_code}")

            if not qr_code:
                # Add the missing qr_code field
                qr_data = f"terminal_id:{terminal_id}"
                update_data = {'qr_code': qr_data}

                success = firebase_service.update_terminal(terminal_id, update_data)
                if success:
                    print(f"  ✅ Added qr_code: {qr_data}")
                else:
                    print(f"  ❌ Failed to update terminal")
            else:
                print(f"  ✅ QR code already exists: {qr_code}")

        print("\n" + "=" * 50)
        print("🎉 QR code fix completed!")

    except Exception as e:
        print(f"❌ Error: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    fix_qr_codes()
