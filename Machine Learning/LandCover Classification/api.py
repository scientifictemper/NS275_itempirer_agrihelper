from keras.preprocessing import image
from tensorflow import keras
import numpy as np
import flask
from flask import request

app = flask.Flask(__name__)
app.config["DEBUG"] = True

@app.route('/get_land_type', methods=['POST'])
def home():
    print('Request ', request.files)
    imagefile = request.files['image']
    new_model = keras.models.load_model('area_identification.h5')
    size = 64
    test_image = image.load_img(imagefile, target_size=(size, size))
    test_image = image.img_to_array(test_image)
    test_image = np.expand_dims(test_image, axis=0)
    test_image.reshape(size, size, 3)
    result = new_model.predict(test_image, batch_size=1)
    index = np.argmax(result[0])
    print(index, type(index))
    labels = ['AnnualCrop', 'Forest', 'Highway', 'Industrial', 'Residential', 'SeaLake']
    return labels[int(index)]
app.run()
