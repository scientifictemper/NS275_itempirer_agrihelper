from six.moves import urllib
import os
import cv2
import numpy as np
from PIL import Image
from keras.models import load_model
from keras.preprocessing import image
import matplotlib.pyplot as plt
import tensorflow as tf
from tensorflow.keras.preprocessing.image import ImageDataGenerator

BATCH_SIZE = 32
image_size = 64
train_image_generator = ImageDataGenerator()
test_image_generator = ImageDataGenerator()
base_dir = 'Images'
train_dir = os.path.join(base_dir, 'Train')
validation_dir = os.path.join(base_dir, 'Validation')

train_data = train_image_generator.flow_from_directory(batch_size = BATCH_SIZE,
                                                     directory = train_dir,
                                                     shuffle = True,
                                                     target_size = (image_size, image_size),
                                                     class_mode = 'sparse')

validation_data = test_image_generator.flow_from_directory(batch_size = BATCH_SIZE,
                                                          directory = validation_dir,
                                                          shuffle = False,
                                                          target_size = (image_size, image_size),
                                                          class_mode = 'sparse')


model = tf.keras.Sequential([
    tf.keras.layers.Conv2D(32, (3,3), activation='relu', input_shape=(64, 64, 3)),
    tf.keras.layers.MaxPooling2D(2, 2),

    tf.keras.layers.Conv2D(64, (3,3), activation='relu', input_shape=(64, 64, 3)),
    tf.keras.layers.MaxPooling2D(2, 2),

    tf.keras.layers.Conv2D(128, (3,3), activation='relu', input_shape=(64, 64, 3)),
    tf.keras.layers.MaxPooling2D(2, 2),

    tf.keras.layers.Conv2D(128, (3,3), activation='relu', input_shape=(64, 64, 3)),
    tf.keras.layers.MaxPooling2D(2, 2),

    tf.keras.layers.Flatten(),
    tf.keras.layers.Dense(512, activation='relu'),
    tf.keras.layers.Dense(6, activation='softmax')
])

model.compile(loss='sparse_categorical_crossentropy', optimizer='adam', metrics=['accuracy'])

EPOCHS = 50

history = model.fit_generator(
    train_data,
    steps_per_epoch = int(np.ceil(15300/BATCH_SIZE)),
    epochs = EPOCHS,
    validation_data = validation_data,
    validation_steps = int(np.ceil(1700/BATCH_SIZE))
)
print(history)
#Save the Model
model.save('area_identification.h5')
model.save(filepath='saved_model/')

from keras.preprocessing import image
from tensorflow import keras
import numpy as np
new_model = keras.models.load_model('area_identification.h5')
size=64
test_image = image.load_img('test2.png', target_size=(size, size))
test_image = image.img_to_array(test_image)
test_image = np.expand_dims(test_image, axis=0)
test_image.reshape(size, size, 3)
result = new_model.predict(test_image, batch_size=1)
index_min = np.argmax(result[0])
print(index_min)
#0 - AnnualCrop
#1 - Forest
#2 - Highway
#3 - Industrial
#4 - Residential
#5 - SeaLake
