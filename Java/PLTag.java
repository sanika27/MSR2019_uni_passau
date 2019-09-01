package nz.ac.vuw.ecs.msr19;

public enum PLTag {

    assembly, c,  cpp, csharp, go, java_, javascript, matlab, objective_c, php, plsql, python, r, ruby, perl, sql, swift ;


    public static PLTag from(String name) {
        if (name.equals("java")) return PLTag.java_;
        else if (name.equals("c#")) return PLTag.csharp;
        else if (name.equals("c++")) return PLTag.cpp;
        else if (name.equals("objective-c")) return PLTag.objective_c;
        else return PLTag.valueOf(name);
    }

    @Override
    public String toString() {
        if (this==java_) return "java";
        else if (this==csharp) return "c#";
        else if (this==objective_c) return "objective-c";
        else if (this==cpp) return "c++";
        else return super.toString();
    }

}
