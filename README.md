# Overview

SocialNetworks (a working project name, subject to change) is a Java library that provides classes and methods to manipulate, analyze, and visualize data from [Stack Exchange](http://stackexchange.com/), an online question and answer network with a variety of distinct communities dedicated to topics ranging from [math](http://math.stackexchange.com/) to [music](http://music.stackexchange.com/) to [the workplace](http://workplace.stackexchange.com/) to [religion](http://buddhism.stackexchange.com/).

# Reason for this API's Existence

To thrive in today's interconnected and information overloaded world, it's critical to understand not only where to find and how to process useful information, but also how to make information useful to others.  Studying and learning from Stack Exchange website data can help imrpove each of these skills.

Each active Stack Exchange website (of which there are about 150) is a community of experts and enthusiasts answering questions related to specific topics, so these communities are excellent places to find and procure useful information about their respective topics.

Stack Exchange provides public "data dumps" for each Stack Exchange community.  These data dumps consist of XML files with information about all of a community's users, questions, answers, comments, and more.

This API helps programmers interact with the Stach Exchange Data Dump.

# How to Use this API

[to be updated]

[get the files in this repo]
[get a Stack Exchange Data Dump]
[create a new StackExchangeTopicGraph topicGraph]
[load a community's data into memory by using util.GraphLoader.populateStackExchangeTopicGraph(topicGraph, directory) on a directory containing a specific community's XML files]
[do whatever you need to do / answer whatever questions you need to answer using the provided methods etc.]

# Where to Find the Data (Stack Exchange Data Dump)

You can download the [March 1, 2016 data dump here](https://archive.org/details/stackexchange) and learn about [the data dump's schema here](http://meta.stackexchange.com/questions/2677/database-schema-documentation-for-the-public-data-dump-and-sede).

As of the time of this writing, the March 1, 2016 data dump is the latest data dump.  If this is not the case and you would like an updated data dump, Google can help with that.

# Acknowledgements

The idea for this project and the vast majority of code is mine.

However, this project was initiated as the captstone for the class "Capstone: Analyzing (Social) Network Data", which is the last class of the Java Programming: Object Oriented Design of Data Structures Specialization offered by University of California, San Diego (UCSD) on Coursera.

Although no code and no instruction was directly provided for this project, the USCD MOOC Team, consisting of (at least) Mia Minnes, Christine Alvarado, Leo Porter, Alec Brickner, and Adam Setters, provided some skeleton starter code in the following files to help with completing a few programming assignments early in the course:

- graph.Graph.java  
- graph.CapGraph.java (a basic outline, no implementation)
- util.GraphLoader.java (but only the loadGraph() method)
- graph.grader.Grader.java
- graph.grader.EgoGrader.java
- graph.grader.SCCGrader.java

They also provided several data files that can be found in the /data directory.

Many thanks to the UCSD Mooc Team for putting these materials together, providing them under the open source MIT License, and sparking some ideas.

# License

The following paragraph is the license information for the all of the code and files added to this GitHub repository after the inital commit and all of the changes made to the code and files provided in the initial commit:

__**__  
__**__ Created by Ryan William Connor in April 2016.  
__**__ Copyright © 2016 Ryan William Connor.  All rights reserved.  
__**__

The following section is the license information for the files and code as seen in the initial commit of this GitHub repository:

__**__  
__**__ The software developed specifically as part of the Coursera/UCSD Course is  
__**__ distributed under the MIT License.  
__**__  
__**__ The MIT License (MIT)  
__**__  
__**__ Copyright (c) 2015 UC San Diego Intermediate Software Development MOOC team  
__**__  
__**__ Permission is hereby granted, free of charge, to any person obtaining a copy  
__**__ of this software and associated documentation files (the "Software"), to  
__**__ deal in the Software without restriction, including without limitation the  
__**__ right to use, copy, modify, merge, publish, distribute, sublicense, and/or  
__**__ sell copies of the Software, and to permit persons to whom the Software is  
__**__ furnished to do so, subject to the following conditions:  
__**__  
__**__ The above copyright notice and this permission notice shall be included in  
__**__ all copies or substantial portions of the Software.  
__**__  
__**__ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
__**__ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
__**__ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  
__**__ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
__**__ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING  
__**__ FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER  
__**__ DEALINGS IN THE SOFTWARE.  
__**__
