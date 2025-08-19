#!/usr/bin/env python3
"""
Debug script to check QR code data in Firebase
This will help identify why QR codes aren't being found during scanning
"""

import firebase_admin
from firebase_admin import credentials, firestore
import json

def debug_qr_codes():
    """Debug QR code data in Firebase"""
    
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
    
    print("🔍 Debugging QR Code Data in Firebase")
    print("=" * 50)
    
    try:
        # Get all terminals
        terminals_ref = db.collection('terminals')
        terminals = terminals_ref.stream()
        
        terminal_count = 0
        qr_code_count = 0
        
        for terminal_doc in terminals:
            terminal_count += 1
            terminal_data = terminal_doc.to_dict()
            terminal_id = terminal_doc.id
            
            print(f"\n{terminal_count}. Terminal: {terminal_data.get('name', 'Unknown')}")
            print(f"   Document ID: {terminal_id}")
            print(f"   terminal_id field: {terminal_data.get('terminal_id', 'MISSING')}")
            
            # Check QR code data
            qr_code = terminal_data.get('qr_code')
            qr_code_url = terminal_data.get('qr_code_url')
            
            if qr_code:
                qr_code_count += 1
                print(f"   ✅ qr_code: '{qr_code}'")
                print(f"   qr_code length: {len(qr_code)}")
                print(f"   qr_code type: {type(qr_code)}")
                
                # Check if it follows expected format
                if qr_code.startswith('terminal_id:'):
                    extracted_id = qr_code.split('terminal_id:')[1]
                    print(f"   ✅ Format correct, extracted ID: '{extracted_id}'")
                    print(f"   ID matches document ID: {extracted_id == terminal_id}")
                    print(f"   ID matches terminal_id field: {extracted_id == terminal_data.get('terminal_id')}")
                else:
                    print(f"   ❌ Format incorrect! Expected 'terminal_id:XXXXX'")
            else:
                print(f"   ❌ qr_code: MISSING")
            
            if qr_code_url:
                print(f"   ✅ qr_code_url: {qr_code_url}")
            else:
                print(f"   ❌ qr_code_url: MISSING")
            
            # Show all fields for debugging
            print(f"   All fields: {list(terminal_data.keys())}")
            
            # Show raw data for first few terminals
            if terminal_count <= 3:
                print(f"   Raw data: {json.dumps(terminal_data, indent=2, default=str)}")
        
        print(f"\n" + "=" * 50)
        print(f"📊 Summary:")
        print(f"   Total terminals: {terminal_count}")
        print(f"   Terminals with QR codes: {qr_code_count}")
        print(f"   Missing QR codes: {terminal_count - qr_code_count}")
        
        if qr_code_count == 0:
            print(f"\n❌ NO QR CODES FOUND!")
            print(f"   This explains why scanning doesn't work.")
            print(f"   Run your populate_data command with --with-qr flag:")
            print(f"   python manage.py populate_data --with-qr")
        elif qr_code_count < terminal_count:
            print(f"\n⚠️  Some terminals missing QR codes.")
            print(f"   You may need to regenerate QR codes for missing terminals.")
        else:
            print(f"\n✅ All terminals have QR codes!")
        
        # Test QR code lookup
        if qr_code_count > 0:
            print(f"\n🧪 Testing QR Code Lookup:")
            print(f"   Let's test if we can find terminals by QR code...")
            
            # Get first terminal with QR code for testing
            test_terminals = terminals_ref.limit(1).stream()
            for test_doc in test_terminals:
                test_data = test_doc.to_dict()
                test_qr = test_data.get('qr_code')
                
                if test_qr:
                    print(f"   Testing with QR code: '{test_qr}'")
                    
                    # Test exact match query
                    query_result = terminals_ref.where('qr_code', '==', test_qr).get()
                    found_docs = list(query_result)
                    
                    print(f"   Query result: {len(found_docs)} documents found")
                    
                    if len(found_docs) > 0:
                        print(f"   ✅ QR code lookup works!")
                        found_data = found_docs[0].to_dict()
                        print(f"   Found terminal: {found_data.get('name')}")
                    else:
                        print(f"   ❌ QR code lookup failed!")
                        print(f"   This indicates a data consistency issue.")
                break
    
    except Exception as e:
        print(f"❌ Error: {e}")
        print(f"Make sure you have:")
        print(f"1. Correct Firebase credentials")
        print(f"2. Firebase Admin SDK installed: pip install firebase-admin")
        print(f"3. Proper service account key file path")

if __name__ == "__main__":
    debug_qr_codes()
