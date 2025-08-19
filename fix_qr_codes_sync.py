#!/usr/bin/env python3
"""
Fix QR codes to ensure proper synchronization between Django and Android
This script will ensure all terminals have the correct qr_code format
"""

import firebase_admin
from firebase_admin import credentials, firestore

def fix_qr_codes():
    """Fix QR code data to ensure Android app compatibility"""
    
    # Initialize Firebase (adjust the path to your service account key)
    try:
        # If already initialized, get the existing app
        app = firebase_admin.get_app()
    except ValueError:
        # Initialize if not already done
        # You may need to adjust this path to your service account JSON file
        cred = credentials.Certificate("path/to/your/serviceAccountKey.json")
        app = firebase_admin.initialize_app(cred)
    
    db = firestore.client()
    
    print("🔧 Fixing QR Code Data for Android Compatibility")
    print("=" * 50)
    
    try:
        # Get all terminals
        terminals_ref = db.collection('terminals')
        terminals = terminals_ref.stream()
        
        terminal_count = 0
        fixed_count = 0
        
        for terminal_doc in terminals:
            terminal_count += 1
            terminal_data = terminal_doc.to_dict()
            terminal_id = terminal_doc.id
            name = terminal_data.get('name', 'Unknown')
            
            print(f"\n{terminal_count}. Processing: {name}")
            print(f"   Document ID: {terminal_id}")
            
            updates = {}
            needs_update = False
            
            # Ensure terminal_id field exists and matches document ID
            stored_terminal_id = terminal_data.get('terminal_id')
            if not stored_terminal_id or stored_terminal_id != terminal_id:
                updates['terminal_id'] = terminal_id
                needs_update = True
                print(f"   ✅ Setting terminal_id: {terminal_id}")
            
            # Ensure qr_code field exists with correct format
            qr_code = terminal_data.get('qr_code')
            expected_qr_code = f"terminal_id:{terminal_id}"
            
            if not qr_code or qr_code != expected_qr_code:
                updates['qr_code'] = expected_qr_code
                needs_update = True
                print(f"   ✅ Setting qr_code: {expected_qr_code}")
            else:
                print(f"   ✅ QR code already correct: {qr_code}")
            
            # Ensure is_active field exists
            if 'is_active' not in terminal_data:
                updates['is_active'] = True
                needs_update = True
                print(f"   ✅ Setting is_active: True")
            
            # Apply updates if needed
            if needs_update:
                terminal_doc.reference.update(updates)
                fixed_count += 1
                print(f"   ✅ Updated terminal: {name}")
            else:
                print(f"   ✅ No updates needed for: {name}")
        
        print(f"\n" + "=" * 50)
        print(f"📊 Summary:")
        print(f"   Total terminals processed: {terminal_count}")
        print(f"   Terminals updated: {fixed_count}")
        print(f"   Terminals already correct: {terminal_count - fixed_count}")
        
        if fixed_count > 0:
            print(f"\n✅ QR code synchronization complete!")
            print(f"   Your Android app should now be able to scan QR codes properly.")
        else:
            print(f"\n✅ All terminals were already properly configured!")
        
        # Verify the fix by testing QR code lookup
        print(f"\n🧪 Verifying QR Code Lookup:")
        test_terminals = terminals_ref.limit(1).stream()
        for test_doc in test_terminals:
            test_data = test_doc.to_dict()
            test_qr = test_data.get('qr_code')
            
            if test_qr:
                print(f"   Testing QR code: '{test_qr}'")
                
                # Test exact match query
                query_result = terminals_ref.where('qr_code', '==', test_qr).get()
                found_docs = list(query_result)
                
                print(f"   Query result: {len(found_docs)} documents found")
                
                if len(found_docs) > 0:
                    print(f"   ✅ QR code lookup verification successful!")
                else:
                    print(f"   ❌ QR code lookup verification failed!")
            break
    
    except Exception as e:
        print(f"❌ Error: {e}")
        print(f"Make sure you have:")
        print(f"1. Correct Firebase credentials")
        print(f"2. Firebase Admin SDK installed: pip install firebase-admin")
        print(f"3. Proper service account key file path")

if __name__ == "__main__":
    fix_qr_codes()
