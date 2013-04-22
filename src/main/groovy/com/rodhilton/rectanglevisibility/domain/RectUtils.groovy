package com.rodhilton.rectanglevisibility.domain

class RectUtils {
    static String printRectangles(Rectangle... rects) {
        StringBuilder sb = new StringBuilder();
        int width=180
        int height=90
        for(int i=height;i>0;i--) {
            sb.printf("%3.2f | ", [i/(double)height])
            for(int j=0;j<width;j++){
                String toPrint = " "
                for(int k=1;k<=rects.size();k++) {
                    Rectangle rect = rects[k-1]
                    def nw = [(int)(rect.north*height), (int)(rect.west*width)]
                    def se = [(int)(rect.south*height), (int)(rect.east*width)]

                    if(i==nw[0]&&j==nw[1]) {
                        toPrint = "O"
                    } else if(i==se[0]&&j==se[1]) {
                        toPrint = "O"
                    } else if(i==nw[0]&&j==se[1]) {
                        toPrint = "."
                    } else if(i==se[0]&&j==nw[1]) {
                        toPrint = "'"
                    } else if ( (i==se[0]&&j<se[1]&&j>nw[1]) || (i==nw[0]&&j<se[1]&&j>nw[1]) ) {
                        if(rects.size() >= 10) {
                            if(j==nw[1]+1 && i==nw[0]) toPrint = ""+(k/10).toInteger()
                            else if(j==nw[1]+2 && i==nw[0]) toPrint = ""+(k%10).toInteger()
                            else toPrint = "—"
                        } else {
                            if(j==nw[1]+1 && i==nw[0]) toPrint = ""+k
                            else toPrint = "—"
                        }
                    } else if ( (j==se[1]&&i>se[0]&&i<nw[0]) || (j==nw[1]&&i<=nw[0]&&i>se[0])) {
                        toPrint = "|"
                    }

                }

                sb.print(toPrint)
            }
            sb.print("\n")
        }
        return sb.toString()
    }
}
