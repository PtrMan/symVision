This project is an attempt of a (re)implementation of [Phaeaco](https://www.foundalis.com/res/diss_research.html)
based on the [Phd Dissertation](https://www.foundalis.com/res/Foundalis_dissertation.pdf) from [Harry Foundalis](https://www.foundalis.com/).

# Motivation

Adaptive systems should have the capability to build, introspect and reason about the visual composition of a perceived scene.
They should also be able to update(revise) this knowledge.
It is strictly necessary that the system is able to decide which knowledge is not useful and can be disgarded.
This all has to happen in
 * a unsupervised fashion
 * online while the system is running
 * open ended manner
all under the assumption of insufficient knowledge and resources (AIKR).

It has to happen
* unsupervised, because a teacher can't be a assumed inside a robot :)
* online, there can't be pauses to "train" machine learning models
* open ended, because a real system must be open to new perceptions, knowledge and tasks at any time
* under AIKR because resources, inclusive (computation) time are finite and usually not sufficient for the tasks at hand

The author is not aware of many deep learning (DL) or machine learning(ML) systems which fullfill these requirements even in principle.
It was possible since the 50s to train neural networks for the recognition of specific features in a supervised way, but this is not sufficient for systems which have to operate in the real world.
Somehow the whole field of machine vision ignored research for unsupervised methods which can be applied to vision.

Foundalis decided to not release his implementation due to ethical concerns. The main author of this project decided to reimplement Foundalis ideas because the capabilities are just to useful for future systems.

# Implementation

## Implemented features based on Phaeaco

### Vision processes
| Process        | Purpose           | Implementationstate  | current issues | tested? |
|---|---|---|---|---| 
| A      | sampling of points from raw black and white image | 99% done | none | not directly because it is working fine |
| D      | proposal of lines from points | working, buggy    | currently implementation leads to lines which are to short because it is hard to find points on the line by chance | visual tests will be added |
| | | | | |
| B      | computation of altitude of points                            | partial | none? | no |
| C      | identifies if a point is a point of the endo- or exoskeleton | minimal? | ? | no |
| E      | finds line intersections                                     | partial | none? | no |
| F      | Sends traces in imagespace for samples which are deeper than a threshold | partial | none? | no |
| G      | curve detection                                              | partial, ~20%? | ? | no |
| H      | tries to combine linedetectors                               | partial | ? | no |
| M      | identify M features                                          | partial, ~10%? | ? | no |
| Z      | zooms in to resolve small features                           | partial | ? | no |

Not implemented

* Process i
* Process O
* Process K
* Process P
* Process R
* Process Q
* Process S

# Graph

Graph matching is just partially implemented and partially tested.

## Nodes

Phaeaco stored and revised knowledge of the perceived scene and known structures as graphs.
It revised the graph to enrich it with perceived or known knowledge.

Implemented
* AttributeNode
* FeatureNode
* NumeriosityNode
* PlatonicPrimitiveInstanceNode
* PlatonicPrimitiveNode

# Codelets

Phaeaco processed modifications of the graph with a codelet based mechanism under AIKR. This was based on research from the FARG group.
This implementation uses the same mechanism as described in Foundalis's dissertation.

Implemented
* Angle
* Barycenter
* EndPoint
* LineSegmentLength
* LineSegmentSlope
* SearchAndFuseRoughtlyEqualElements

# currently not implemented features based on Phaeaco

* full graph matching
* a lot of graph nodes
* a lot of codelets and the interaction of codelets and the graph
