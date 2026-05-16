const STORAGE_KEY = 'bbUser';

export function clearExpiredSession(navigate) {
    localStorage.removeItem(STORAGE_KEY);
    alert('Your guest session has expired. Please log in again.');
    navigate('/login');
}
