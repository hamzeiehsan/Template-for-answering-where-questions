# Answering where questions using generic geographic information
Abstract -- Automatic generation of answers to where-questions is a challenge to current Web search and question answering systems. While human-generated answers of where-questions are short, selective, and informative, machine responses are typically provided in a form of ranked documents and text snippets. Several approaches have been proposed to answer questions using the information available in documents and knowledge-bases. These methods assume that the answers can be retrieved completely inside the sources without any further modifications. In this research, we present an approach to generate answers to where-questions by selecting relevant pieces of information that can form responses similar to human-generated answers. We derive and use patterns of generic geographic information (e.g., type, scale, and prominence) encoded from the largest available machine comprehension dataset, MS MARCO v2.1. In our approach, the toponyms in the questions and answers of the dataset are encoded into sequences of generic information. Next, sequence prediction methods are used to model the relation between the generic information in the questions and their answers. Finally, we evaluate the performance of the predictive models generating generic form of answers to where-questions. The proposed approach can be used to augment querying databases and knowledge graphs to identify relevant information and to construct responses similar to human-generated answers.

## Installation
The implementation is mainly developed in Java ([Oracle Java](https://www.oracle.com/technetwork/java/javase/downloads/index.html) - version 8) using [Maven](https://maven.apache.org/download.cgi) v3 for library management.
Several [R](https://www.r-project.org/) scripts are used for postprocessing of the results and generating plots. The R scripts can be found in the [R-scripts folder](R-scripts).
### Prerequisites
Install a JEE Java IDE (-- e.g., [Intellij IDEA](https://www.jetbrains.com/idea/download/)), dependencies will be downloaded from Web repositories automatically.
List of java dependencies:

| Dependency        | Version           | Description  |
| ------------- |:-------------:| -----:|
| SMPF      | 2.40 | Sequence Mining Library |
| slf4j      | 1.7.30 | Logging Framework |
| jackson      | 2.10.1      |   JSON Serialization |
| gson | 2.8.5     |    JSON I/O |
| geonames | 1.0     |    Gazetteer Lookup |
| nominatim-api | 3.4     |    Gazetteer Lookup |
| junit | 4.11    |   Unit Testing |

R libraries should be installed manually (uncomment installation code in the header of each script).

| Dependency        | Version           | Description  |
| ------------- |:-------------:| -----:|
| arules      | 1.6-4 | Association Rule Mining |
| TraMineR      | 2.0-14 | Sequence Mining |
| TraMineRextras      | 0.4.6 | Sequence Mining |
| classInt      | 0.4-2 | Class Intervals (Jenk) |
| ggplot2      | 3.2.1 | Generating Plots |
| ggpubr      | 0.2.4 | Generating Vector Plots |
| gridExtra      | 2.3 | Generating Plots |
| RColorBrewer      | 1.1-2 | Color Coding |
| pastecs      | 1.3.21 | Data Manipulation |
| Matrix      | 1.2-18 | Data Manipulation |
| qlcMatrix      | 0.9.7      |   Data Manipulation |
| plyr      | 1.8.5 | Data Manipulation |
| dplyr      | 0.8.3 | Data Manipulation |
| data.table      | 1.12.8 | Data Manipulation |
| stringr      | 1.4.0 | Data Manipulation |
| fpc      | 2.2-4 | Data Manipulation |

Before running the code, you should download Microsoft MS MARCO dataset v2.1 and put the dev, train and eval datasets inside the dataset folder.
## Running Source Code
Import the project into your JEE IDE. Wait until maven fetch the libraries from Web repositories.
Check the [configuration file](src/main/resources/properties.properties) and change the parameters
Run the Workflow java file in the [root package folder](src/main/java/anonymous/).

### Configuration

Set the dataset folder, output folder (processed data, gazetteers local folder, sequence generation and prediction) in [configuration file](src/main/resources/properties.properties). 

### Workflow

The workflow includes the following steps which can be run in [batch](src/main/java/anonymous/Workflow.java) or separate (e.g., [preprocessing](src/main/java/anonymous/dataset/Analyze.java)) ways.


### Packages
Source code contains 5 main package which are listed and described below:
1. dataset: reading dataset files and preprocessing
2. parser: reading parse results and filtering toponym-based where-questions
3. gazetteers: disambiguation of extracted place names 
4. sequence: generating type, scale and prominence sequences
5. predict: sequence prediction 

Each package has a runnable source file which can be run separately.
* [dataset](src/main/java/anonymous/dataset/Analyze.java)
* [parser](src/main/java/anonymous/gazetteers/geonames/Analyze.java)
* [gazetteers](src/main/java/anonymous/gazetteers/osm/Analyze.java) 
* [sequence](src/main/java/anonymous/sequence/Analyze.java), [postprocessing](src/main/java/anonymous/sequence/PostProcessing.java)
* [predict](src/main/java/anonymous/predict/ipredict/controllers/PredictionWorkflowController.java)

### Build
To build the runnable jar file from the source run the following Maven command:


```
clean dependency:copy-dependencies insall
```

Then move the jar file (where_questions-1.0.jar) and dependency folder from the target folder to the root folder.

The resulted jar file of the project can be run using the following command.


```
java -jar where_questions-1.0-jar-with-dependencies
```

The results can be found in their folders (check the configuration file: properties.properties).


## Running R-Scripts
R scripts can be found in [R-Scripts folder](R-scripts). In this folder, you could run the [batch script](R-scripts/Batch_Run.R) or you could test the scripts separately. Note that the scripts should be run after running java codes because their inputs is the outputs of the java-based program. In the following list the scripts are briefly introduced:

* [AssociationRuleMining.R](R-scripts/AssociationRuleMining.R): association rule mining based on sequences of place types, scale and prominence of questions and their answers.
* [Batch_Run.R](R-scripts/Batch_Run.R): batch file that runs all of the other scripts sequentially.
* [importanceDistribution.R](R-scripts/importanceDistribution.R): analysis of importance value extracted from OpenStreeMap results.
* [OrdinalAnalysis.R](R-scripts/OrdinalAnalysis.R): Analysis of ordinal values (scale and prominence)
* [OrdinalRepresentation.R](R-scripts/OrdinalRepresentation.R): Analysis of ordinal values (scale and prominence sequences)
* [prominence-categorical-distribution.R](R-scripts/prominence-categorical-distribution.R): Analysis of prominence sequences
* [scale-categorical-distribution.R](R-scripts/scale-categorical-distribution.R): Analysis of scale sequences
* [type-categorical-distribution.R](R-scripts/type-categorical-distribution.R): Analysis of type sequences
* [tsp_complexity.R](R-scripts/tsp_complexity.R):

## Built With

* [Java 8](https://www.oracle.com/technetwork/java/javase/downloads/index.html) - The programming language used
* [R 3.6.2](https://www.r-project.org/) - The scripting language used
* [Maven](https://maven.apache.org/) - Dependency Management
* [Intellij IDEA 2019.1.4](https://www.jetbrains.com/idea/download/) - Used to generate RSS Feeds


## Authors

* Ehsan Hamzei 
* Stephan Winter
* Martin Tomko

## License

This project is licensed under the MIT License.

## Acknowledgments

* Some parts of the code in prediction package is borrowed from [IPredict Project](https://github.com/tedgueniche/IPredict) (a sub-project of [SMPF](https://www.philippe-fournier-viger.com/spmf/)) and is extended to suit our goals.
