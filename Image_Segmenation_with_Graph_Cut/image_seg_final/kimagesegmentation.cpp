#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <utility>
#include <queue>
#include <unordered_map>
#include <map>
#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include <sstream>
#include <iomanip>
#include <math.h>

class Graph {
public:
    std::map<int,std::map<int,int> > adjacencyList;
    void addEdge(int source, int dest, int weight )
    {
        if ( adjacencyList.find(source) == adjacencyList.end() )
        {
            std::map<int,int> edge;
            edge.insert(std::pair<int,int>(dest,weight));
            adjacencyList.insert(std::pair<int, std::map<int,int> >(source,edge));
        }
        else
        {
            //std::map<int,int>& edgeList = adjacencyList.at(source);
            adjacencyList[source].insert(std::pair<int,int>(dest,weight));
        }
    }
    
    void numberOfEdges()
    {
        int size = 0;
        for ( auto& pair:adjacencyList )
        {
            size += pair.second.size();
        }
        std::cout<<"number of edges are "<<size<<"\n";
    }
    
    //void displa
    // We might need some functions but it's better we directly access from adjacencyList rather through function call;
};


int breadthFirstSearch(Graph& residualGraph, int source, int sink, std::vector<int>& parent, std::vector<bool>& reached, bool print)
{
    // We add one because our graph contains only adjacencyList;
    // May be we will change this in the future
    size_t V = residualGraph.adjacencyList.size();
    //std::vector<bool> reached(residualGraph.adjacencyList.size(),false);
    std::queue <int> queue;
    queue.push(source);
    reached[source] = true;
    parent[source] = -1;
    
    while (!queue.empty())
    {
        int u = queue.front();
        queue.pop();
        std::map<int, int>& neighbors = residualGraph.adjacencyList[u];
        //for (int v=0; v<V; v++)
        //{
        for(auto& edgeWeightPair:neighbors )
        {
            if (reached[edgeWeightPair.first]==false && edgeWeightPair.second > 0)
            {
                if (print)
                    std::cout << "neigbouring edge" << edgeWeightPair.first << std::endl;
                queue.push(edgeWeightPair.first);
                parent[edgeWeightPair.first] = u;
                reached[edgeWeightPair.first] = true;
            }
        }
    }
    if(reached[sink])
    {
        return true;
    }
    return false;
}



// method returns max flow using ford_fulkerson with breadth first search
std::vector<int> Ford_Fulkerson (Graph& graph, int source, int sink, int width, int height, int size)
{
    Graph* prevGraph = new Graph();
    prevGraph->adjacencyList = graph.adjacencyList;
    std::vector<bool> reached(graph.adjacencyList.size(),false);
    int u, v, maxFlow=0;
    //int parent[V];
    std::vector<int> parent(graph.adjacencyList.size());
    // check if there is a path from source to sink
    while (breadthFirstSearch(graph, source, sink, parent,reached, false))
    {
        // Find the directed edge with minimum capacity in the s-t path
        int minCapacityEdge = 10000000; // set some high value in the beginning;
        for (v=sink; v!=source; v=parent[v])
        {
            u = parent[v];
            minCapacityEdge = std::min(minCapacityEdge, graph.adjacencyList[u][v]);
        }
        // push flow equal to the minCapacityEdge in the s-t path
        // update the graph, increasing flow decreasing the capacity on forward direction
        // and increasing capacity on the revese directions
        for (v=sink; v != source; v=parent[v])
        {
            u = parent[v];
            graph.adjacencyList[u][v] -= minCapacityEdge;
            graph.adjacencyList[v][u] += minCapacityEdge;
        }
        
        // Update the flow with amount pushed.
        maxFlow += minCapacityEdge;
        std::fill(reached.begin(),reached.end(),false);
    }
    
    //breadthFirstSearch(graph, source, sink, parent,reached, true);
    
    /*for (int i = 0; i < prevGraph->adjacencyList.size(); i++)
     {
     for (int j = 0; j < prevGraph->adjacencyList.size(); j++)
     {
     if (reached[i] && !reached[j] && prevGraph->adjacencyList[i][j])
     {
     std::cout << i << " - " << j << std::endl;
     }
     }
     }*/
    
    int foregroundCount = 0;
    int backgroundCount = 0;
    
    std::vector<int> segmentArray(size);
    for (int i=0; i< prevGraph->adjacencyList.size()-1; ++i)
    {
        int rowVal = i/width;
        int colVal  = i%width;
        if ( reached[i+1] )
        {
            //binaryImage.at<uchar>(rowVal,colVal) = 0;
            //foregroundCount++;
            segmentArray[i]=1;
        }
        else
        {
            // backgroundCount++;
            segmentArray[i]=-1;
        }
    }
    //std::cout << "foreground count is " << foregroundCount << std::endl;
    //std::cout << "background count is " << backgroundCount << std::endl;
    
    //
    
    return segmentArray;
    //return maxFlow;
}

Graph* createGraph ( int width, int height, int penalty, std::vector<int>& likelihoodArray )
{
    int imageSize = width*height;
    std::cout << "The image size passed is " << imageSize << std::endl;
    Graph* g = new Graph();
    for (int i=0; i<width*height; ++i )
    {
        g->addEdge(0,i+1,likelihoodArray.at(i));
        g->addEdge(i+1,(width*height)+1,10-likelihoodArray.at(i));
    }
    for ( int i=1; i<= imageSize; ++i )
    {
        int rownum = (i-1)/width;
        int colnum = (i-1)%width;
        
        //The above rownum and colnum work when we start with 0,0 as our indices;
        //since here our vertices start with 1,1
        rownum = rownum+1;
        colnum = colnum+1;
        // first row
        if ( rownum == 1 )
        {
            // horizontal next pixel
            if ( i != width )
                g->addEdge(i,i+1,penalty);
            
            // horizontal previous pixel
            if ( colnum != width )
                g->addEdge(i,i-1,penalty);
            
            // vertical below pixel
            g->addEdge(i,i+width,penalty);
            
        }
        else if ( colnum == 1 )  // first column
        {
            // we don't have to worry about the edge condition for pixel 1 since it will go to previous if loop;
            g->addEdge(i,i+1,penalty);
            g->addEdge(i,i-width,penalty);
            
            if ( rownum != height)             //last row;
                g->addEdge(i,i+width,penalty);
            
        }
        else if ( rownum == height )
        {
            if ( colnum != width )
                g->addEdge(i,i+1,penalty);
            
            g->addEdge(i,i-width,penalty);
            // no check required here since colnum 1 will go to previous loop
            g->addEdge(i,i-1, penalty);
        }
        else if ( colnum == width )
        {
            g->addEdge(i,i+width,penalty);
            g->addEdge(i,i-width,penalty);
            g->addEdge(i,i-1,penalty);
        }
        else
        {
            g->addEdge(i,i+width,penalty);
            g->addEdge(i,i-width,penalty);
            g->addEdge(i,i-1,penalty);
            g->addEdge(i,i+1,penalty);
        }
    }
    g->numberOfEdges();
    return g;
}


int main(int argc, const char * argv[]) {
    
    std::string imagename;
    int gmmornot = 0;  // default is gmm
    int numberofClusters = 2; // default is 2 cluster
    std::string filename(" ");
    std::cout << "Ford Fulkerson implementation begin() " << std::endl;
    if (argc < 2)
    {
        std::cout << "Invalid number of arguments" << std::endl;
        std::cout << "Please provide image name and an integer (0 or 1! 0 for GMM, 1 for K-Means ) and number of segments " << std::endl;
        return 0;
    }
    if (argc == 2)
    {
        imagename = std::string(argv[1]);
    }
    else if(argc == 3)
    {
        imagename = std::string(argv[1]);
        gmmornot = std::atoi(argv[2]);
    }
    else if (argc == 4)
    {
        imagename = std::string(argv[1]);
        gmmornot = std::atoi(argv[2]);
        numberofClusters = std::atoi(argv[3]);
    }
    std::string baselocation = ("./");
    std::string imageFile = baselocation + std::string("image/") + std::string(imagename);
    //std::string filename = baselocation + "likelihood.py";
    if (gmmornot == 0 )
        filename = baselocation + "gmm_clustering.py ";
    else
        filename = baselocation + "k-likelihood.py ";
    std::string command = "python ";
    command += filename;
    command += imageFile + std::string(" ") + std::to_string(numberofClusters);
    system(command.c_str());
    
    std::cout << "The command used to call the python file is " << command << std::endl;
    
    cv::Mat image;
    image = cv::imread(imageFile);
    std::cout<<image.size().width<<"\n";
    std::cout<<image.size().height<<"\n";
    if (!image.data)
    {
        std::cout << "Unable to read image file " << std::endl;
    }
    
    // read the kmeans output and store it as a vector
    //std::string file = baselocation + std::string("k-data.txt");
    std::string file = baselocation + std::string("output.txt");
    std::ifstream label(file.c_str());
    //std::vector<string> k_data;
    //std::vector<int> likelihoodArray;
    int size=image.size().height * image.size().width;
    int width=image.size().width;
    int height=image.size().height;
    //int width=3;
    //int height=3;
    //int size=9;
    int k=numberofClusters;
    std::vector<std::vector<int> > k_likelihoodArray;
    
    //std::ifstream file("k-data.txt");
    std::string input;
    int i=0;
    if(label.is_open())
    {
        while (std::getline(label,input))
        {
            std::vector<int> line(k);
            size_t beg=0;
            size_t end=0;
            int i = 0;
            std::istringstream iss(input);
            while (iss) {
                std::string word;
                iss >> word;
                line[i] = std::atoi(word.c_str());
                ++i;
            }
            k_likelihoodArray.push_back(line);
        }
    }
    
    std::cout << "The likeilihood vector values are" << std::endl;
    for (auto& vec:k_likelihoodArray)
    {
        for (auto& val:vec)
        {
            std::cout << val << " ";
        }
        std::cout << std::endl;
    }
    
    int column;
    std::vector<int> currentsegmentprob(size);
    for (column=0; column<size; column++)
    {
        currentsegmentprob.at(column)=k_likelihoodArray[column][0];
    }
    
    
    Graph* inputGraph = createGraph(image.cols,image.rows,4,currentsegmentprob);
    inputGraph->addEdge(inputGraph->adjacencyList.size(),inputGraph->adjacencyList.size(),0);
    std::vector<int> segmatrix;
    segmatrix=Ford_Fulkerson(*inputGraph,0,inputGraph->adjacencyList.size()-1,image.cols, image.rows,size);
    
    
    std::cout<<"run till here \n";
    for (column=0; column<size; column++){
        if(segmatrix[column]==1)
        {
            k_likelihoodArray[column][0]=10;
            for(i=1; i<k; i++)
                k_likelihoodArray[column][i]=0;
        }
        else
        {
            k_likelihoodArray[column][0]=0;
        }
    }
    //int j;
    int j,row;
    
    for (i=1; i<k-1; i++)
    {
        std::cout<<"no\n";
        //normalization
        std::vector<int> sum(size,0);
        for (row=0; row<size; row++)
            for (j=i; j<k; j++)
                sum[row]=sum[row]+k_likelihoodArray[row][j];
        
        
        for (row=0; row<size; row++){
            for (j=i; j<k; j++){
                if (k_likelihoodArray[row][j]!=0){
                    k_likelihoodArray[row][i]=(10*k_likelihoodArray[row][i])/sum[row];
                }
            }
        }
        for (row=0; row<size; row++){
            currentsegmentprob.at(row)=k_likelihoodArray[row][i];
        }
        
        //fordfulkerson here again
        
        
        //Graph* inputGraph = createGraph(height,width,4,currentsegmentprob);
        
        inputGraph = createGraph(image.cols,image.rows,4,currentsegmentprob);
        inputGraph->addEdge(inputGraph->adjacencyList.size(),inputGraph->adjacencyList.size(),0);
        std::vector<int> temp=Ford_Fulkerson(*inputGraph,0,inputGraph->adjacencyList.size()-1,image.cols, image.rows,size);
        for (row=0; row<size; row++)
        {
            //deletion of nodes from prob matrix (assigning zero prob to all other values)
            if(temp.at(row)==1)
            {
                k_likelihoodArray[row][i]=10;
                segmatrix.at(row)=i+1;
                for(j=i+1; j<k; j++)
                    k_likelihoodArray[row][j]=0;
            }
            else
            {
                k_likelihoodArray[row][i]=0;
            }
        }
        
    }
    
    for (row=0;row<size;row++)
    {
        if (segmatrix.at(row)==-1)
            segmatrix.at(row)=k;
    }
    
    row=0;
    cv::Mat binaryImage = cv::Mat(height,width,CV_8UC1);
    //binaryimage.at<uchar>(rowVal,colVal) = 255/3;
    
    for(i=0;i<height;i++){
        for (j=0;j<width;j++){
            binaryImage.at<uchar>(i,j) = (255*segmatrix.at(row))/k;
            //std::cout<<(255*segmatrix.at(row))/k<<"\n";
            row++;
        }
    }
    
    cv::imshow("Segmented Image",binaryImage);
    cv::waitKey(0);
    return 0;
    
}

