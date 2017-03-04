import java.io.FileNotFoundException;
import java.io.File;
import java.util.Scanner;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.ArrayDeque;
public class Analyzer
{
    private HashMap<Integer,Integer> nodesByIndex;
    private HashMap<Integer,Integer> indexByNodes;
    private int[][] edgeMatrix;
    private int[][] shortestPathWeights;
    private ArrayList<ArrayList<Integer>>[][] shortestPaths;
    private int[] degreeCentralities;
    private int numVertices; 
    private float [] katzCentralities;
    private float [] closenessCentralities;
    private float [] betweenessCentralities;
    public Analyzer(String filename) throws FileNotFoundException
    {
        File file = new File(filename);
        Scanner fileReader = new Scanner(file);
        fileReader.useDelimiter(" |\\n");//seperate by spaces and new lines
        numVertices = 0;
        nodesByIndex = new HashMap<Integer,Integer>();
        indexByNodes = new HashMap<Integer,Integer>();
        //Add distinct lists of nodes and indexes using HashMaps so that conversion between a node and an index can be done easily
        while(fileReader.hasNextInt())
        {
            int current = fileReader.nextInt();
            if(!nodesByIndex.containsKey(current))
            {
                nodesByIndex.put(current,numVertices);
                indexByNodes.put(numVertices++,current);
            }
        }
        fileReader.close();
        fileReader = new Scanner(file);//read the file again using the knowledge of the numVertices
        fileReader.useDelimiter(" |\\n");//seperate by spaces and new lines
        edgeMatrix = new int[numVertices][numVertices];//set up an empty edgeMatrix with the correct length
        degreeCentralities = new int[numVertices];//setup the degree Centralities with the correct length
        while(fileReader.hasNextInt())
        {
            int v1 = fileReader.nextInt();
            int v2 = fileReader.nextInt();
            int i1 = nodesByIndex.get(v1);
            int i2 = nodesByIndex.get(v2);
            //Setup the edge Matrix and record the degree Centrality
            if(edgeMatrix[i1][i2] != 1)
            {
                degreeCentralities[i1]++;
                degreeCentralities[i2]++;
                edgeMatrix[i1][i2] = 1;
                edgeMatrix[i2][i1] = 1;
            }
        }
    }
    public int getNodeFromIndex(int index)
    {
        if(!indexByNodes.containsKey(index))
            return -1;
        return indexByNodes.get(index);
    }
    public int[][] getEdgeMatrix()
    {
        return edgeMatrix;
    }
    public int getNumVertices()
    {
        return numVertices;
    }
    public int[] getDegreeCentralities()
    {
        return degreeCentralities;
    }
    public int[][] getShortestPathWeights()
    {
        return shortestPathWeights;
    }
    public float[] getClosenessCentralities()
    {
        return closenessCentralities;
    }
    public float[] getKatzCentralities()
    {
        return katzCentralities;
    }
     public float[] getBetweenessCentralities()
    {
        return betweenessCentralities;
    }

    public void calculateShortestPaths(float katzConstant)
    {
        closenessCentralities = new float[numVertices];//setup the closeness Centralities with the correct length
        shortestPathWeights = new int[numVertices][numVertices];//setup the list of shortest paths for every vertex
        katzCentralities = new float[numVertices];//setup the katz Centralities with the correct length
        betweenessCentralities = new float[numVertices];//setup the Betweeness Centralities with the correct length
        //Use Brandes-like efficient algorithm O(EV^2) to retrieve the shortest paths to be used and the betweeness centrality: 
        // Brandes algorithm : http://algo.uni-konstanz.de/publications/b-fabc-01.pdf (Accessed: 8/06/2016)
        java.util.Stack<Integer> verticesByDistance;
        ArrayList shortestPaths[] = new ArrayList[numVertices];
        float sigma[] = new float[numVertices];
        float[] delta = new float[numVertices];
        int distances[];
        java.util.Queue<Integer> queue;
        for(int sourceVertex = 0; sourceVertex < numVertices; sourceVertex++)
        {
            verticesByDistance = new java.util.Stack<Integer>();
            distances = new int[numVertices];
            for(int i = 0; i<numVertices; i++)
            {
                shortestPaths[i] = new ArrayList();
                sigma[i] = 0;
                distances[i] = -1;
            }
            sigma[sourceVertex] = 1;
            distances[sourceVertex] = 0;
            queue = new ArrayDeque<Integer>();
            queue.add(sourceVertex);
            int currentVertex;
            while(!queue.isEmpty())
            {
                currentVertex = queue.remove();
                verticesByDistance.push(currentVertex);
                for(int currentNeighbor = 0; currentNeighbor< numVertices; currentNeighbor++)
                {
                    if(edgeMatrix[currentVertex][currentNeighbor] == 1)
                    {
                        if(distances[currentNeighbor]<0)
                        {
                            queue.add(currentNeighbor);
                            distances[currentNeighbor] = distances[currentVertex]+1;
                        }
                        if(distances[currentNeighbor] == distances[currentVertex]+1)
                        {
                            sigma[currentNeighbor] += sigma[currentVertex];
                            shortestPaths[currentNeighbor].add(currentVertex);
                        }
                    }
                }
            }
            for(int i = 0; i< numVertices; i++)
            {
                delta[i] = 0;
            }
            while(!verticesByDistance.isEmpty())
            {
                currentVertex = verticesByDistance.pop();
                java.util.Iterator<Integer> pathIterator = shortestPaths[currentVertex].iterator();
                int currentPathStep;
                while(pathIterator.hasNext())
                {
                    currentPathStep = pathIterator.next();
                    delta[currentPathStep] += (float)((float)(sigma[currentPathStep]))/(float)(sigma[currentVertex])*(float)(delta[currentVertex]+1);
                }
                if(currentVertex != sourceVertex)
                    betweenessCentralities[currentVertex] += (float)delta[currentVertex];
            }
            shortestPathWeights[sourceVertex] = distances;
        }
        //Calculate the Katz and Closeness Centrality together in O(V^2)
        for(int i = 0; i<numVertices; i++)
        {
            for(int j = 0; j<numVertices; j++)
            {
                katzCentralities[i] += ((float)degreeCentralities[j])*Math.pow(katzConstant,shortestPathWeights[i][j]);
                closenessCentralities[i] += shortestPathWeights[i][j];
            }
            closenessCentralities[i] = 1/closenessCentralities[i];
        }
    }
}
