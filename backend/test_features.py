from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_all():
    # 1. Health
    res = client.get('/health')
    assert res.status_code == 200, res.text
    print('1. Health check OK:', res.json())

    # 2. Curriculum Catalog
    res = client.get('/curriculum')
    assert res.status_code == 200
    nodes = res.json()
    print(f'2. Curriculum OK: {len(nodes)} nodes across Classes 6-9')
    assert len(nodes) >= 10, "Should have rich catalog across classes 6-9"

    # 3. Registration
    res = client.post('/auth/register', json={
        'name': 'Priyanka Dash',
        'email': 'priyanka@nmiet.edu.in',
        'password': 'password123',
        'class_grade': 8,
        'board': 'ODISHA_STATE',
        'preferred_language': 'BILINGUAL'
    })
    assert res.status_code == 200, res.text
    user = res.json()
    token = user['token']
    print('3. Auth Register OK:', user['name'], user['email'])

    # 4. Login
    res = client.post('/auth/login', json={
        'email': 'priyanka@nmiet.edu.in',
        'password': 'password123'
    })
    assert res.status_code == 200, res.text
    print('4. Auth Login OK:', res.json()['token'])

    # 5. Google Authentication
    res = client.post('/auth/google', json={
        'email': 'priyanka.google@gmail.com',
        'name': 'Priyanka Google',
        'photo_url': 'https://example.com/avatar.png',
        'class_grade': 7,
        'board': 'CBSE'
    })
    assert res.status_code == 200, res.text
    print('5. Auth Google OK:', res.json()['name'], res.json()['auth_provider'])

    # 6. Live Content Generation (HuggingFace references)
    res = client.post('/content/live-generate?class_grade=7&board=CBSE')
    assert res.status_code == 200, res.text
    gen = res.json()
    print(f'6. Live Dataset Gen OK: {gen["item_count"]} items for Class 7 CBSE')

    # 7. Audio Speech Transcription
    res = client.post('/tutor/transcribe-audio?language=or-IN')
    assert res.status_code == 200, res.text
    transcript_preview = res.json()['transcript'].encode('ascii', errors='backslashreplace').decode('ascii')
    print('7. Audio Transcription OK:', transcript_preview)

    print('\n>>> ALL 7 BACKEND FEATURES VERIFIED AND PASSING! <<<')

if __name__ == '__main__':
    test_all()
