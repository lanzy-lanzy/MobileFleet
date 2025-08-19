#!/usr/bin/env python3
"""
Simple script to add missing qr_code fields to existing terminals
This script directly uses Firebase Admin SDK
"""

import firebase_admin
from firebase_admin import credentials, firestore
import os

def add_qr_codes_to_terminals():
    print("🔧 Adding missing qr_code fields to existing terminals...")
    print("=" * 60)
    
    try:
        # Initialize Firebase (adjust the path to your service account key)
        if not firebase_admin._apps:
            # You'll need to update this path to your Firebase service account JSON file
            cred = credentials.Certificate("path/to/your/firebase-service-account.json")
            firebase_admin.initialize_app(cred)
        
        db = firestore.client()
        
        # Get all terminals
        terminals_ref = db.collection('terminals')
        terminals = terminals_ref.stream()
        
        count = 0
        updated = 0
        
        for terminal_doc in terminals:
            count += 1
            terminal_data = terminal_doc.to_dict()
            terminal_id = terminal_doc.id
            name = terminal_data.get('name', 'Unknown')
            qr_code = terminal_data.get('qr_code')
            
            print(f"\n{count}. Processing: {name}")
            print(f"   Terminal ID: {terminal_id}")
            print(f"   Current qr_code: {qr_code}")
            
            if not qr_code:
                # Add the missing qr_code field
                qr_data = f"terminal_id:{terminal_id}"
                
                # Update the document
                terminal_doc.reference.update({
                    'qr_code': qr_data
                })
                
                print(f"   ✅ Added qr_code: {qr_data}")
                updated += 1
            else:
                print(f"   ✅ QR code already exists")
        
        print("\n" + "=" * 60)
        print(f"🎉 Completed! Processed {count} terminals, updated {updated} terminals")
        print("\nNow try scanning your QR codes again!")
        
    except Exception as e:
        print(f"❌ Error: {e}")
        print("\nMake sure to:")
        print("1. Update the path to your Firebase service account JSON file")
        print("2. Install firebase-admin: pip install firebase-admin")

if __name__ == "__main__":
    add_qr_codes_to_terminals()
