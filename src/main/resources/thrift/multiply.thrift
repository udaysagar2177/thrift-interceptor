namespace java thrift.interceptor.example.multiply

typedef i32 int
typedef i64 long


struct Options {
   1: long timeout;
   2: int clientId;
}

service MultiplicationService {
  int multiply(1:int n1, 2:int n2, 3:Options options);
}