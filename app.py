from flask import Flask

app = Flask(__name__)

@app.route('/')
def home():
    return "<h1>Credit Card Fraud Detection Using Machine Learning: Transaction Analysis for Real-Time Threat Identification</h1>"

if __name__ == '__main__':
    app.run(debug=True)
