# MSR2019_uni_passau
Study into Language Identification of Stack Overflow programming language tags

This repository contains the scripts to reproduce the results for studying the programming language tags associated with Stack Overflow snippets.

Steps:
1. User provided tags on posts should be fetched by quering the SOTorrent dataset  version: 2019_06_21
2. linguist_tags.rb : Uses GitHub's Linguist classifier to detect the programming languages for the code snippets. This can be achieved by running: gem install github-linguist

Using the two CSV files from previous steps the data processing will be done by the java file
3. ComputeAlphas.java: To compute the alpha values
4. ComputTagCooccurence.java: To compute the the tag co-occurence values for the user provided tags.
5. ComputeLinguistCooccurence.java: To compute the tag co-occurence values for the linguist tags.
