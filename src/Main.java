import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class Main {
    private static class Ride implements Comparable<Ride> {
        public final int a, b, x, y, s, f;
        public final int id;

        public Ride(int a, int b, int x, int y, int s, int f, int id) {
            this.a = a;
            this.b = b;
            this.x = x;
            this.y = y;
            this.s = s;
            this.f = f;
            this.id = id;
        }

        @Override
        public int compareTo(Ride other) {
            /*int diff1 = Math.abs(x-a) - Math.abs(y-b);
            int diff2 = Math.abs(other.x-other.a) - Math.abs(other.y-other.b);
            if (diff1 != diff2) {
                if (diff1 > diff2) {return -1;}
                else {return 1;}
            }
            return 0;*/
            if (this.s != other.s) {
                if (this.s < other.s) {return -1;}
                else {return 1;}
            }
            return 0;
        }
    }

    private static int R, C, F, N, B, T;
    private static List<Ride> rides;

    //GA parameters
    private static Random generator;
    private static final int M = 200;
    private static final double PC = 1;
    private static final double PM = 0.01;

    private static void readInput(BufferedReader reader) throws Exception {
        String[] line = reader.readLine().split(" ");
        R = Integer.parseInt(line[0]);
        C = Integer.parseInt(line[1]);
        F = Integer.parseInt(line[2]);
        N = Integer.parseInt(line[3]);
        B = Integer.parseInt(line[4]);
        T = Integer.parseInt(line[5]);

        rides = new ArrayList<>();
        for (int i=1; i<=N; ++i) {
            line = reader.readLine().split(" ");
            rides.add(new Ride(
                    Integer.parseInt(line[0]),
                    Integer.parseInt(line[1]),
                    Integer.parseInt(line[2]),
                    Integer.parseInt(line[3]),
                    Integer.parseInt(line[4]),
                    Integer.parseInt(line[5]),
                    i-1
            ));
        }
        Collections.sort(rides);
    }

    private static class Assignment {
        public final List<Integer> assigedCars;
        public final long score;

        Assignment(List<Integer> assignedCars, long score) {
            this.assigedCars = assignedCars;
            this.score = score;
        }

        private static Assignment from(List<Integer> ncars) {
            int[] la = new int[F];
            int[] lr = new int[F];

            long score = 0;

            for (int i=0; i<rides.size(); ++i) {
                int j = ncars.get(i);
                if (j<0) {continue;}
                int px = (lr[j] >= 0) ? rides.get(lr[j]).x : 0;
                int py = (lr[j] >= 0) ? rides.get(lr[j]).y : 0;

                int cx = rides.get(i).a;
                int cy = rides.get(i).b;

                int st = la[j] + Math.abs(cx-px) + Math.abs(cy-py);
                if (st <= rides.get(i).f) {
                    int ft = Math.max(st, rides.get(i).s) + Math.abs(rides.get(i).x - cx) +
                            Math.abs(rides.get(i).y - cy);
                    if (ft <= rides.get(i).f) {
                        score += (Math.abs(rides.get(i).x - rides.get(i).a) +
                                Math.abs(rides.get(i).y - rides.get(i).b));
                        la[j] = ft;
                        lr[j] = i;
                        if (st <= rides.get(i).s) {
                            score += B;
                        }
                    } else {
                        ncars.set(i, -1);
                    }
                }
            }
            return new Assignment(ncars, score);
        }

        private static Assignment fromMutation(Assignment a) {
            List<Integer> ncars = new ArrayList<>();
            for (int i=0; i<a.assigedCars.size(); ++i) {
                if (generator.nextDouble() < PM) {
                    ncars.add((int)(generator.nextDouble() * F));
                } else {
                    ncars.add(a.assigedCars.get(i));
                }
            }
            return from(ncars);
        }

        private static Assignment from(Assignment a1, Assignment a2, int tidx) {
            List<Integer> ncars = new ArrayList<>();

            for (int i=0; i<a1.assigedCars.size(); ++i) {
                if (generator.nextDouble() < 0.5) {
                    ncars.add(a1.assigedCars.get(i));
                } else {
                    ncars.add(a2.assigedCars.get(i));
                }
            }

            return from(ncars);
        }

        private static Assignment createRandom() {
            int[] la = new int[F];
            int[] lr = new int[F];
            Arrays.fill(lr, -1);
            List<Integer> assignedCars = new ArrayList<>(rides.size());

            long score = 0;

            for (int i=0; i<rides.size(); ++i) {
                List<Integer> cars = new ArrayList<>();
                Map<Integer, Integer> carToLastTime = new HashMap<>();
                Map<Integer, Integer> carToStartTime = new HashMap<>();
                for (int j=0; j<F; ++j) {
                    int px = (lr[j] >= 0) ? rides.get(lr[j]).x : 0;
                    int py = (lr[j] >= 0) ? rides.get(lr[j]).y : 0;

                    int cx = rides.get(i).a;
                    int cy = rides.get(i).b;

                    int st = la[j] + Math.abs(cx-px) + Math.abs(cy-py);
                    if (st <= rides.get(i).f) {
                        int ft = Math.max(st, rides.get(i).s) + Math.abs(rides.get(i).x - cx) +
                                Math.abs(rides.get(i).y - cy);
                        if (ft <= rides.get(i).f) {
                            cars.add(j);
                            carToStartTime.put(j, st);
                            carToLastTime.put(j, ft);
                        }
                    }
                }

                if (!cars.isEmpty()) {
                    int idx = (int)(generator.nextDouble() * cars.size());
                    int carId = cars.get(idx);
                    assignedCars.add(carId);
                    la[carId] = carToLastTime.get(carId);
                    lr[carId] = i;

                    if (carToStartTime.get(carId) <= rides.get(i).s) {
                        score += B;
                    }
                    score += (Math.abs(rides.get(i).x - rides.get(i).a) +
                            Math.abs(rides.get(i).y - rides.get(i).b));
                } else {
                    assignedCars.add(-1);
                }
            }
            return new Assignment(assignedCars, score);
        }

        public void print() {
            System.out.println("*******Assignment********");
            for (int i=0; i<assigedCars.size(); ++i) {
                System.out.println(rides.get(i).id + " " + assigedCars.get(i));
            }
            System.out.println("Score + " + score);
            System.out.println("*************************");
        }

        public void printToFile(BufferedWriter writer) throws Exception {
            List<Integer>[] car_rides = new ArrayList[F];
            for (int i=0; i<assigedCars.size(); ++i) {
                if (assigedCars.get(i) < 0) {continue;}
                if (car_rides[assigedCars.get(i)] == null) {
                    car_rides[assigedCars.get(i)] = new ArrayList<>();
                }
                car_rides[assigedCars.get(i)].add(rides.get(i).id);
            }
            for (int i=0; i<F; ++i) {
                if (car_rides[i] != null) {
                    writer.write(car_rides[i].size() + "");
                    for (int j=0; j<car_rides[i].size(); ++j)
                        writer.write(" " + car_rides[i].get(j));
                } else {
                    writer.write("0");
                }
                writer.write("\n");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("e.in"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("e.out"));

        readInput(reader);

        List<Assignment> population = new ArrayList<>();
        for (int i=1; i<=M; ++i) {
            generator = new Random(System.currentTimeMillis());
            population.add(Assignment.createRandom());
        }

        long maxScore = 0;
        Assignment best = null;

        for (int it=1; it<=100; ++it) {
            generator = new Random(System.currentTimeMillis());
            System.out.println(it);

            long total = 0;
            for (int i=0; i<population.size(); ++i) {
                total += population.get(i).score;
            }

            for (int i=0; i<population.size(); ++i) {
                if (population.get(i).score > maxScore) {
                    maxScore = population.get(i).score;
                    best = population.get(i);
                }
            }

            List<Assignment> new_population = new ArrayList<>();
            for (int i=1; i<=M; ++i) {
                double rand = generator.nextDouble();

                double cur = 0;
                int idx=0;
                while (cur < rand && idx < population.size()) { //roulette
                    cur += (population.get(idx).score / total);
                    ++idx;
                }
                --idx;
                new_population.add(population.get(idx));
            }

            population = new_population;

            for (int i=0; i<(population.size()-1); ++i) { //1-point crossover
                if (generator.nextDouble() < PC) {
                    int rand = (int) (generator.nextDouble() * rides.size());
                    Assignment child1 = Assignment.from(population.get(i), population.get(i + 1), rand);
                    Assignment child2 = Assignment.from(population.get(i + 1), population.get(i), rand);
                    if (child1.score > population.get(i).score) {
                        population.set(i, child1);
                    }
                    if (child2.score > population.get(i+1).score) {
                        population.set(i + 1, child2);
                    }
                }
            }

            for (int i=0; i<population.size(); ++i) { //mutation
                Assignment nassigment = Assignment.fromMutation(population.get(i));
                population.set(i, nassigment);
            }

        }

        best.print();
        best.printToFile(writer);
        writer.close();
        reader.close();
    }
}
