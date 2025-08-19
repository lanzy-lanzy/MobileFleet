#!/usr/bin/env python3
"""
Script to add sample QR code URLs to existing terminals in Firebase
This will help test the QR code display functionality in the Android app
"""

import firebase_admin
from firebase_admin import credentials, firestore
import os

def main():
    print("🔧 Adding sample QR code URLs to terminals...")
    
    # Initialize Firebase Admin SDK
    try:
        # Try to get existing app
        app = firebase_admin.get_app()
    except ValueError:
        # Initialize new app if none exists
        cred = credentials.Certificate("path/to/serviceAccountKey.json")  # Update this path
        app = firebase_admin.initialize_app(cred)
    
    db = firestore.client()
    
    # Sample QR code URLs (using placeholder URLs for testing)
    sample_qr_urls = [
        "https://res.cloudinary.com/demo/image/upload/v1234567890/qr_codes/terminal_1.png",
        "https://res.cloudinary.com/demo/image/upload/v1234567890/qr_codes/terminal_2.png", 
        "https://res.cloudinary.com/demo/image/upload/v1234567890/qr_codes/terminal_3.png",
        "https://res.cloudinary.com/demo/image/upload/v1234567890/qr_codes/terminal_4.png",
        "https://res.cloudinary.com/demo/image/upload/v1234567890/qr_codes/terminal_5.png"
    ]
    
    try:
        # Get all terminals
        terminals_ref = db.collection('terminals')
        terminals = list(terminals_ref.stream())
        
        print(f"Found {len(terminals)} terminals")
        
        if not terminals:
            print("❌ No terminals found. Please run the populate_data.py script first.")
            return
        
        # Update each terminal with a sample QR code URL
        for i, terminal_doc in enumerate(terminals):
            terminal_data = terminal_doc.to_dict()
            terminal_id = terminal_doc.id
            name = terminal_data.get('name', 'Unknown')
            current_qr_url = terminal_data.get('qr_code_url', '')
            
            print(f"\n{i+1}. Processing: {name}")
            print(f"   Terminal ID: {terminal_id}")
            print(f"   Current QR URL: {current_qr_url}")
            
            # Add or update QR code URL
            qr_url = sample_qr_urls[i % len(sample_qr_urls)]  # Cycle through URLs
            
            # Ensure qr_code field exists
            qr_code = terminal_data.get('qr_code')
            if not qr_code:
                qr_code = f"terminal_id:{terminal_id}"
            
            update_data = {
                'qr_code_url': qr_url,
                'qr_code': qr_code
            }
            
            # Update the document
            terminal_doc.reference.update(update_data)
            
            print(f"   ✅ Updated QR URL: {qr_url}")
            print(f"   ✅ QR Code: {qr_code}")
        
        print(f"\n🎉 Successfully updated {len(terminals)} terminals with QR code URLs!")
        print("\n📱 You can now test the QR code display in the Android app.")
        
    except Exception as e:
        print(f"❌ Error: {e}")

if __name__ == "__main__":
    main()
