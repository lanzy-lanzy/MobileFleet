import firebase_admin
from firebase_admin import credentials, firestore
from django.conf import settings
from datetime import datetime
import logging

logger = logging.getLogger(__name__)

class FirebaseService:
    _instance = None
    _db = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(FirebaseService, cls).__new__(cls)
            cls._instance._initialize_firebase()
        return cls._instance

    def _initialize_firebase(self):
        """Initialize Firebase Admin SDK"""
        try:
            if not firebase_admin._apps:
                # Use the service account JSON file path
                cred = credentials.Certificate(settings.FIREBASE_SERVICE_ACCOUNT_PATH)
                firebase_admin.initialize_app(cred, {
                    'projectId': settings.FIREBASE_PROJECT_ID
                })
            self._db = firestore.client()
            logger.info("Firebase initialized successfully")
        except Exception as e:
            logger.error(f"Failed to initialize Firebase: {e}")
            self._db = None

    @property
    def db(self):
        """Get Firestore database instance"""
        if self._db is None:
            self._initialize_firebase()
        return self._db

    # Terminal Management
    def create_terminal(self, terminal_data):
        """Create a new terminal in Firestore"""
        try:
            terminal_data['created_at'] = datetime.now()
            terminal_data['updated_at'] = datetime.now()
            doc_ref = self.db.collection('terminals').document()
            terminal_data['terminal_id'] = doc_ref.id
            doc_ref.set(terminal_data)
            logger.info(f"Terminal created: {doc_ref.id}")
            return doc_ref.id
        except Exception as e:
            logger.error(f"Error creating terminal: {e}")
            return None

    def get_terminal(self, terminal_id):
        """Get a terminal by ID"""
        try:
            doc = self.db.collection('terminals').document(terminal_id).get()
            if doc.exists:
                return doc.to_dict()
            return None
        except Exception as e:
            logger.error(f"Error getting terminal {terminal_id}: {e}")
            return None

    def get_all_terminals(self):
        """Get all terminals"""
        try:
            terminals = []
            docs = self.db.collection('terminals').stream()
            for doc in docs:
                terminal_data = doc.to_dict()
                terminal_data['id'] = doc.id
                terminals.append(terminal_data)
            return terminals
        except Exception as e:
            logger.error(f"Error getting terminals: {e}")
            return []

    def update_terminal(self, terminal_id, update_data):
        """Update a terminal"""
        try:
            update_data['updated_at'] = datetime.now()
            self.db.collection('terminals').document(terminal_id).update(update_data)
            logger.info(f"Terminal updated: {terminal_id}")
            return True
        except Exception as e:
            logger.error(f"Error updating terminal {terminal_id}: {e}")
            return False

    def delete_terminal(self, terminal_id):
        """Delete a terminal"""
        try:
            self.db.collection('terminals').document(terminal_id).delete()
            logger.info(f"Terminal deleted: {terminal_id}")
            return True
        except Exception as e:
            logger.error(f"Error deleting terminal {terminal_id}: {e}")
            return False

    # Driver Management
    def create_driver(self, driver_data):
        """Create a new driver in Firestore"""
        try:
            driver_data['created_at'] = datetime.now()
            driver_data['updated_at'] = datetime.now()
            doc_ref = self.db.collection('drivers').document()
            driver_data['driver_id'] = doc_ref.id
            doc_ref.set(driver_data)
            logger.info(f"Driver created: {doc_ref.id}")
            return doc_ref.id
        except Exception as e:
            logger.error(f"Error creating driver: {e}")
            return None

    def get_driver(self, driver_id):
        """Get a driver by ID"""
        try:
            doc = self.db.collection('drivers').document(driver_id).get()
            if doc.exists:
                return doc.to_dict()
            return None
        except Exception as e:
            logger.error(f"Error getting driver {driver_id}: {e}")
            return None

    def get_all_drivers(self):
        """Get all drivers"""
        try:
            drivers = []
            docs = self.db.collection('drivers').stream()
            for doc in docs:
                driver_data = doc.to_dict()
                driver_data['id'] = doc.id
                drivers.append(driver_data)
            return drivers
        except Exception as e:
            logger.error(f"Error getting drivers: {e}")
            return []

    def update_driver(self, driver_id, update_data):
        """Update a driver"""
        try:
            update_data['updated_at'] = datetime.now()
            self.db.collection('drivers').document(driver_id).update(update_data)
            logger.info(f"Driver updated: {driver_id}")
            return True
        except Exception as e:
            logger.error(f"Error updating driver {driver_id}: {e}")
            return False

    def delete_driver(self, driver_id):
        """Delete a driver"""
        try:
            self.db.collection('drivers').document(driver_id).delete()
            logger.info(f"Driver deleted: {driver_id}")
            return True
        except Exception as e:
            logger.error(f"Error deleting driver {driver_id}: {e}")
            return False

    # Trip Management
    def create_trip(self, trip_data):
        """Create a new trip in Firestore"""
        try:
            trip_data['created_at'] = datetime.now()
            trip_data['updated_at'] = datetime.now()
            doc_ref = self.db.collection('trips').document()
            trip_data['trip_id'] = doc_ref.id
            doc_ref.set(trip_data)
            logger.info(f"Trip created: {doc_ref.id}")
            return doc_ref.id
        except Exception as e:
            logger.error(f"Error creating trip: {e}")
            return None

    def get_trip(self, trip_id):
        """Get a trip by ID"""
        try:
            doc = self.db.collection('trips').document(trip_id).get()
            if doc.exists:
                return doc.to_dict()
            return None
        except Exception as e:
            logger.error(f"Error getting trip {trip_id}: {e}")
            return None

    def get_all_trips(self, limit=None):
        """Get all trips with optional limit"""
        try:
            trips = []
            query = self.db.collection('trips').order_by('created_at', direction=firestore.Query.DESCENDING)
            if limit:
                query = query.limit(limit)
            docs = query.stream()
            for doc in docs:
                trip_data = doc.to_dict()
                trip_data['id'] = doc.id
                trips.append(trip_data)
            return trips
        except Exception as e:
            logger.error(f"Error getting trips: {e}")
            return []

    def get_trips_by_status(self, status):
        """Get trips by status"""
        try:
            trips = []
            docs = self.db.collection('trips').where('status', '==', status).stream()
            for doc in docs:
                trip_data = doc.to_dict()
                trip_data['id'] = doc.id
                trips.append(trip_data)
            return trips
        except Exception as e:
            logger.error(f"Error getting trips by status {status}: {e}")
            return []

    def get_trips_by_driver(self, driver_id):
        """Get trips by driver ID"""
        try:
            trips = []
            docs = self.db.collection('trips').where('driver_id', '==', driver_id).stream()
            for doc in docs:
                trip_data = doc.to_dict()
                trip_data['id'] = doc.id
                trips.append(trip_data)
            return trips
        except Exception as e:
            logger.error(f"Error getting trips for driver {driver_id}: {e}")
            return []

    def update_trip(self, trip_id, update_data):
        """Update a trip"""
        try:
            update_data['updated_at'] = datetime.now()
            self.db.collection('trips').document(trip_id).update(update_data)
            logger.info(f"Trip updated: {trip_id}")
            return True
        except Exception as e:
            logger.error(f"Error updating trip {trip_id}: {e}")
            return False

    def delete_trip(self, trip_id):
        """Delete a trip"""
        try:
            self.db.collection('trips').document(trip_id).delete()
            logger.info(f"Trip deleted: {trip_id}")
            return True
        except Exception as e:
            logger.error(f"Error deleting trip {trip_id}: {e}")
            return False

    def get_active_trips(self):
        """Get all active/in-progress trips"""
        return self.get_trips_by_status('in_progress')

    def get_completed_trips(self):
        """Get all completed trips"""
        return self.get_trips_by_status('completed')


# Singleton instance
firebase_service = FirebaseService()