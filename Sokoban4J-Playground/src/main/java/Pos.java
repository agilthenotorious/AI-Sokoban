public class Pos {
    private Integer hash = null;
    int x;
    int y;
    Pos(int x,int y){
        this.x=x;
        this.y=y;
    }
    @Override
    public int hashCode() {
        if (hash == null) {
            hash = 0;
            hash += (290317 * x + 97 * y);
        }
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if(this.hashCode()==o.hashCode())  return true;
        else return false;
    }
}

