__author__ = "Rohan Pandit" 

from algo import algorithm, triTueAlgo, withDelay
import numpy as np
from time import time
from random import randint
from flask import Flask, abort, jsonify, request
from flask_cors import CORS

screenSize = 700

app = Flask(__name__)
CORS(app)

@app.route('/optimize_route', methods=['POST'])
def optimize():
	data = request.get_json(force=True)
	cities = data['matrix']
	path, length = triTueAlgo(cities)
	return jsonify(length = length, path = path)

@app.route('/optimize_with_time', methods=['POST'])
def optimize_with_time():
	data = request.get_json(force = True)
	cities = data['matrix']
	maximumTime = data['maximum_time']
	path, length = withDelay(cities, maximumTime)
	return jsonify(found = not (len(path) == 0), length = length, path = path)

app.run(host='0.0.0.0', port = 8888)


