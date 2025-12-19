from flask import Flask, render_template, request, redirect, url_for, session, flash
import requests

app = Flask(__name__)
app.secret_key = 'bookmyshow_secret_key_2024'

# Java Backend URL
JAVA_BACKEND_URL = 'https://bookmyshow-clone-java-based-1.onrender.com'

def call_backend(endpoint, method='GET', data=None):
    """Helper function to call Java backend API"""
    try:
        url = f"{JAVA_BACKEND_URL}{endpoint}"
        if method == 'GET':
            response = requests.get(url, timeout=5)
        elif method == 'POST':
            response = requests.post(url, json=data, timeout=5)
        return response.json()
    except requests.exceptions.ConnectionError:
        return {'success': False, 'message': 'Cannot connect to Java backend. Make sure it is running on port 8000.'}
    except Exception as e:
        return {'success': False, 'message': str(e)}

@app.route('/')
def home():
    return render_template('index.html')

# ============ USER ROUTES ============

@app.route('/user/login', methods=['GET', 'POST'])
def user_login():
    if request.method == 'POST':
        email = request.form['email']
        password = request.form['password']
        
        # Call Java backend for login
        result = call_backend('/api/user/login', 'POST', {
            'email': email,
            'password': password
        })
        
        if result.get('success'):
            user = result.get('user', {})
            session['user_email'] = email
            session['user_name'] = user.get('name', 'User')
            session['user_id'] = user.get('id', '')
            session['user_type'] = 'user'
            flash('Login successful!', 'success')
            return redirect(url_for('user_dashboard'))
        else:
            flash(result.get('message', 'Invalid email or password'), 'error')
    
    return render_template('user_login.html')

@app.route('/user/register', methods=['GET', 'POST'])
def user_register():
    if request.method == 'POST':
        name = request.form['name']
        email = request.form['email']
        password = request.form['password']
        confirm_password = request.form['confirm_password']
        
        if password != confirm_password:
            flash('Passwords do not match', 'error')
        else:
            # Call Java backend for registration
            result = call_backend('/api/user/register', 'POST', {
                'name': name,
                'email': email,
                'password': password
            })
            
            if result.get('success'):
                flash('Registration successful! Please login.', 'success')
                return redirect(url_for('user_login'))
            else:
                flash(result.get('message', 'Registration failed'), 'error')
    
    return render_template('user_register.html')

@app.route('/user/dashboard')
def user_dashboard():
    if 'user_email' not in session or session.get('user_type') != 'user':
        flash('Please login first', 'error')
        return redirect(url_for('user_login'))
    
    # Get shows for browsing
    shows = call_backend('/api/shows')
    movies = call_backend('/api/movies')
    theatres = call_backend('/api/theatres')
    
    if not isinstance(shows, list):
        shows = []
    if not isinstance(movies, list):
        movies = []
    if not isinstance(theatres, list):
        theatres = []
    
    # Create lookup dictionaries
    movies_dict = {m.get('id'): m for m in movies}
    theatres_dict = {t.get('id'): t for t in theatres}
    
    # Enrich shows with movie and theatre names
    for show in shows:
        movie = movies_dict.get(show.get('movieId'), {})
        theatre = theatres_dict.get(show.get('theatreId'), {})
        show['movieName'] = movie.get('name', 'Unknown')
        show['theatreName'] = theatre.get('name', 'Unknown')
        show['theatreLocation'] = theatre.get('location', '')
    
    return render_template('user_dashboard.html', 
                         user_name=session['user_name'],
                         shows=shows)

# ============ USER BOOKING ROUTES ============

@app.route('/book/<show_id>')
def book_show(show_id):
    if 'user_email' not in session or session.get('user_type') != 'user':
        flash('Please login first', 'error')
        return redirect(url_for('user_login'))
    
    # Get show details and seats
    seat_info = call_backend(f'/api/show-seats?showId={show_id}')
    
    if not seat_info.get('totalSeats'):
        flash('Show not found', 'error')
        return redirect(url_for('user_dashboard'))
    
    # Get show and movie details
    shows = call_backend('/api/shows')
    movies = call_backend('/api/movies')
    theatres = call_backend('/api/theatres')
    
    show = None
    for s in shows if isinstance(shows, list) else []:
        if s.get('id') == show_id:
            show = s
            break
    
    if not show:
        flash('Show not found', 'error')
        return redirect(url_for('user_dashboard'))
    
    movie = None
    for m in movies if isinstance(movies, list) else []:
        if m.get('id') == show.get('movieId'):
            movie = m
            break
    
    theatre = None
    for t in theatres if isinstance(theatres, list) else []:
        if t.get('id') == show.get('theatreId'):
            theatre = t
            break
    
    return render_template('book_seats.html',
                         show=show,
                         movie=movie or {},
                         theatre=theatre or {},
                         seat_info=seat_info)

@app.route('/confirm-booking', methods=['POST'])
def confirm_booking():
    if 'user_email' not in session or session.get('user_type') != 'user':
        return {'success': False, 'message': 'Please login first'}
    
    data = request.get_json()
    show_id = data.get('showId')
    seats = data.get('seats', [])
    
    if not show_id or not seats:
        return {'success': False, 'message': 'Invalid booking data'}
    
    # Call backend to book seats
    result = call_backend('/api/book-seats', 'POST', {
        'showId': show_id,
        'userEmail': session['user_email'],
        'seats': ','.join(map(str, seats))
    })
    
    return result

@app.route('/my-bookings')
def my_bookings():
    if 'user_email' not in session or session.get('user_type') != 'user':
        flash('Please login first', 'error')
        return redirect(url_for('user_login'))
    
    bookings = call_backend(f'/api/bookings?userEmail={session["user_email"]}')
    if not isinstance(bookings, list):
        bookings = []
    
    return render_template('my_bookings.html', 
                         user_name=session['user_name'],
                         bookings=bookings)

@app.route('/user/logout')
def user_logout():
    session.pop('user_email', None)
    session.pop('user_name', None)
    session.pop('user_id', None)
    session.pop('user_type', None)
    flash('Logged out successfully', 'success')
    return redirect(url_for('home'))

# ============ ADMIN ROUTES ============

@app.route('/admin/login', methods=['GET', 'POST'])
def admin_login():
    if request.method == 'POST':
        email = request.form['email']
        password = request.form['password']
        
        # Call Java backend for login
        result = call_backend('/api/admin/login', 'POST', {
            'email': email,
            'password': password
        })
        
        if result.get('success'):
            admin = result.get('admin', {})
            session['admin_email'] = email
            session['admin_name'] = admin.get('name', 'Admin')
            session['admin_id'] = admin.get('id', '')
            session['user_type'] = 'admin'
            flash('Login successful!', 'success')
            return redirect(url_for('admin_dashboard'))
        else:
            flash(result.get('message', 'Invalid email or password'), 'error')
    
    return render_template('admin_login.html')

@app.route('/admin/register', methods=['GET', 'POST'])
def admin_register():
    if request.method == 'POST':
        name = request.form['name']
        email = request.form['email']
        password = request.form['password']
        confirm_password = request.form['confirm_password']
        
        if password != confirm_password:
            flash('Passwords do not match', 'error')
        else:
            # Call Java backend for registration
            result = call_backend('/api/admin/register', 'POST', {
                'name': name,
                'email': email,
                'password': password
            })
            
            if result.get('success'):
                flash('Admin registration successful! Please login.', 'success')
                return redirect(url_for('admin_login'))
            else:
                flash(result.get('message', 'Registration failed'), 'error')
    
    return render_template('admin_register.html')

@app.route('/admin/dashboard')
def admin_dashboard():
    if 'admin_email' not in session or session.get('user_type') != 'admin':
        flash('Please login first', 'error')
        return redirect(url_for('admin_login'))
    
    # Get stats from Java backend
    stats = call_backend('/api/stats')
    
    return render_template('admin_dashboard.html', 
                         admin_name=session['admin_name'],
                         total_theatres=stats.get('total_theatres', 0),
                         total_shows=stats.get('total_shows', 0),
                         total_users=stats.get('total_users', 0))

# ============ LOGOUT ============

@app.route('/logout')
def logout():
    session.clear()
    flash('You have been logged out', 'success')
    return redirect(url_for('home'))

# ============ ADMIN - THEATRE MANAGEMENT ============

@app.route('/admin/add-theatre', methods=['GET', 'POST'])
def add_theatre():
    if 'admin_email' not in session or session.get('user_type') != 'admin':
        flash('Please login first', 'error')
        return redirect(url_for('admin_login'))
    
    if request.method == 'POST':
        name = request.form['name']
        location = request.form['location']
        totalseats = request.form['totalseats']
        tax = request.form.get('tax', '10')
        
        result = call_backend('/api/theatres', 'POST', {
            'name': name,
            'location': location,
            'totalseats': totalseats,
            'tax': tax
        })
        
        if result.get('success'):
            flash('Theatre added successfully!', 'success')
            return redirect(url_for('manage_theatres'))
        else:
            flash(result.get('message', 'Failed to add theatre'), 'error')
    
    return render_template('add_theatre.html')

@app.route('/admin/theatres')
def manage_theatres():
    if 'admin_email' not in session or session.get('user_type') != 'admin':
        flash('Please login first', 'error')
        return redirect(url_for('admin_login'))
    
    theatres = call_backend('/api/theatres')
    if not isinstance(theatres, list):
        theatres = []
    
    return render_template('manage_theatres.html', theatres=theatres)

# ============ ADMIN - MOVIE MANAGEMENT ============

@app.route('/admin/add-movie', methods=['GET', 'POST'])
def add_movie():
    if 'admin_email' not in session or session.get('user_type') != 'admin':
        flash('Please login first', 'error')
        return redirect(url_for('admin_login'))
    
    if request.method == 'POST':
        name = request.form['name']
        genre = request.form['genre']
        duration = request.form['duration']
        language = request.form['language']
        description = request.form.get('description', '')
        
        result = call_backend('/api/movies', 'POST', {
            'name': name,
            'genre': genre,
            'duration': duration,
            'language': language,
            'description': description
        })
        
        if result.get('success'):
            flash('Movie added successfully!', 'success')
            return redirect(url_for('manage_movies'))
        else:
            flash(result.get('message', 'Failed to add movie'), 'error')
    
    return render_template('add_movie.html')

@app.route('/admin/movies')
def manage_movies():
    if 'admin_email' not in session or session.get('user_type') != 'admin':
        flash('Please login first', 'error')
        return redirect(url_for('admin_login'))
    
    movies = call_backend('/api/movies')
    if not isinstance(movies, list):
        movies = []
    
    return render_template('manage_movies.html', movies=movies)

# ============ ADMIN - SHOW MANAGEMENT ============

@app.route('/admin/add-show', methods=['GET', 'POST'])
def add_show():
    if 'admin_email' not in session or session.get('user_type') != 'admin':
        flash('Please login first', 'error')
        return redirect(url_for('admin_login'))
    
    # Get theatres and movies for dropdown
    theatres = call_backend('/api/theatres')
    movies = call_backend('/api/movies')
    
    if not isinstance(theatres, list):
        theatres = []
    if not isinstance(movies, list):
        movies = []
    
    if request.method == 'POST':
        movieId = request.form['movieId']
        theatreId = request.form['theatreId']
        showtime = request.form['showtime']
        price = request.form['price']
        
        result = call_backend('/api/shows', 'POST', {
            'movieId': movieId,
            'theatreId': theatreId,
            'showtime': showtime,
            'price': price
        })
        
        if result.get('success'):
            flash('Show added successfully!', 'success')
            return redirect(url_for('manage_shows'))
        else:
            flash(result.get('message', 'Failed to add show'), 'error')
    
    return render_template('add_show.html', theatres=theatres, movies=movies)

@app.route('/admin/shows')
def manage_shows():
    if 'admin_email' not in session or session.get('user_type') != 'admin':
        flash('Please login first', 'error')
        return redirect(url_for('admin_login'))
    
    shows = call_backend('/api/shows')
    if not isinstance(shows, list):
        shows = []
    
    return render_template('manage_shows.html', shows=shows)

if __name__ == '__main__':
    app.run(debug=True, port=5000)
