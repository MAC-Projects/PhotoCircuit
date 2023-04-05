# PhotoCircuit

PhotoCircuit is an Android application dedicated to the recognition of photographs of simple hand-drawn circuit schematics and the generation of the corresponding [CircuiTikZ](https://ctan.org/pkg/circuitikz?lang=en) code, which is then automatically uploaded to [TermBin](https://termbin.com/).

To accomplish this objective, it makes use of:
* Custom computer vision algorithms implemented through OpenCV, meant to identify circuit element positions and the segments that connect them;
* A custom Convolutional Neural Network built with TensorFlow (through the Keras API) to classify the circuit elements.

The "PhotoCircuit" directory in this repository can be imported into Android Studio and run directly.

We uploaded the customized dataset we used [on Kaggle](https://www.kaggle.com/datasets/altermetax/hand-drawn-circuit-elements) and is based on [this pre-existing one](https://www.kaggle.com/datasets/moodrammer/handdrawn-circuit-schematic-components).

The model can be trained using the IPython Notebook at [training.ipynb](/training.ipynb).

A Python version of the circuit element detection algorithm can be found at [elem_detector.ipynb](/elem_detector.ipynb).

A more thorough explanation of the inner workings of the application (in Italian) can be found at [Relazione.pdf](/Relazione.pdf).

## Sample

Given this photo:

![Photo of the original circuit](/pictures/photo.png)

the application applies an adaptive thresholding algorithm:

![Picture after thresholding](/pictures/threshold.png)

then, after several additional steps, it detects the segments that connect the circuit elements:

![Detected segments](/pictures/segments.png)

It uses the information obtained so far, together with the CNN's classification of the circuit elements, to generate the following TeX code:

```tex
\begin{circuitikz}[american,x=0.01cm,y=0.01cm]
\draw (361, 485) to [short] (531, 485);
\draw (106, 709) to [short] (70, 709);
\draw (465, 439) to [short] (465, 483);
\draw (368, 709) to [short] (182, 709);
\draw (70, 485) to [short] (113, 485);
\draw (531, 485) to [short] (531, 563);
\draw (227, 485) to [short] (316, 485);
\draw (274, 485) to [short] (227, 485);
\draw (70, 485) to [short] (70, 539);
\draw (316, 485) to [short] (274, 485);
\draw (274, 548) to [short] (274, 485);
\draw (531, 709) to [short] (480, 709);
\draw (531, 650) to [short] (531, 709);
\draw (70, 656) to [short] (70, 709);
\draw (274, 658) to [short] (274, 709);
\draw (182, 709) to [short] (274, 709);
\draw (274, 709) to [short] (368, 709);
\draw (465, 439) node[eground]{};
\draw (361, 485) to [C] (316, 485);
\draw (113, 485) to [V] (227, 485);
\draw (531, 563) to [diode,invert] (531, 650);
\draw (70, 539) to [R] (70, 656);
\draw (274, 548) to [R] (274, 658);
\draw (368, 709) to [L] (480, 709);
\draw (106, 709) to [battery2,invert] (182, 709);
\end{circuitikz}
```

Once compiled, the above code appears as follows:

![Resulting compiled LaTeX](/pictures/circuitikz.png)
