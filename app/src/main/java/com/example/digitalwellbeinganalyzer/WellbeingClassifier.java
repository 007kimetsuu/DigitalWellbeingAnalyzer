package com.example.digitalwellbeinganalyzer;
public class WellbeingClassifier {
    public static double[] score(double[] input) {
        double[] var0;
        if (input[0] <= 0.5) {
            if (input[0] <= -0.5) {
                if (input[1] <= 2.5) {
                    if (input[4] <= 0.5) {
                        if (input[1] <= 1.5) {
                            var0 = new double[] {1.0, 0.0, 0.0};
                        } else {
                            if (input[5] <= 0.5) {
                                var0 = new double[] {1.0, 0.0, 0.0};
                            } else {
                                if (input[2] <= 5.5) {
                                    var0 = new double[] {1.0, 0.0, 0.0};
                                } else {
                                    var0 = new double[] {0.8823529411764706, 0.11764705882352941, 0.0};
                                }
                            }
                        }
                    } else {
                        if (input[5] <= 0.5) {
                            if (input[2] <= 10.5) {
                                var0 = new double[] {1.0, 0.0, 0.0};
                            } else {
                                var0 = new double[] {0.9666666666666667, 0.03333333333333333, 0.0};
                            }
                        } else {
                            var0 = new double[] {0.7096774193548387, 0.2903225806451613, 0.0};
                        }
                    }
                } else {
                    if (input[5] <= 0.5) {
                        if (input[6] <= 0.5) {
                            var0 = new double[] {0.9777777777777777, 0.022222222222222223, 0.0};
                        } else {
                            var0 = new double[] {0.9375, 0.0625, 0.0};
                        }
                    } else {
                        var0 = new double[] {0.5714285714285714, 0.42857142857142855, 0.0};
                    }
                }
            } else {
                if (input[1] <= 1.5) {
                    if (input[5] <= 0.5) {
                        if (input[4] <= 0.5) {
                            if (input[1] <= 0.5) {
                                if (input[2] <= 7.5) {
                                    var0 = new double[] {0.9974619289340102, 0.0025380710659898475, 0.0};
                                } else {
                                    var0 = new double[] {0.9726027397260274, 0.0273972602739726, 0.0};
                                }
                            } else {
                                if (input[7] <= 0.5) {
                                    var0 = new double[] {0.9629629629629629, 0.037037037037037035, 0.0};
                                } else {
                                    var0 = new double[] {0.7357723577235772, 0.26422764227642276, 0.0};
                                }
                            }
                        } else {
                            if (input[1] <= 0.5) {
                                if (input[6] <= 0.5) {
                                    var0 = new double[] {0.5185185185185185, 0.48148148148148145, 0.0};
                                } else {
                                    var0 = new double[] {0.34615384615384615, 0.6538461538461539, 0.0};
                                }
                            } else {
                                if (input[7] <= 0.5) {
                                    var0 = new double[] {0.0, 1.0, 0.0};
                                } else {
                                    var0 = new double[] {0.0, 0.9615384615384616, 0.038461538461538464};
                                }
                            }
                        }
                    } else {
                        if (input[1] <= 0.5) {
                            if (input[7] <= 0.5) {
                                if (input[2] <= 6.5) {
                                    var0 = new double[] {0.7741935483870968, 0.22580645161290322, 0.0};
                                } else {
                                    var0 = new double[] {0.4166666666666667, 0.5833333333333334, 0.0};
                                }
                            } else {
                                if (input[2] <= 6.5) {
                                    var0 = new double[] {0.3898305084745763, 0.6101694915254238, 0.0};
                                } else {
                                    var0 = new double[] {0.07692307692307693, 0.9230769230769231, 0.0};
                                }
                            }
                        } else {
                            if (input[7] <= 0.5) {
                                if (input[2] <= 5.5) {
                                    var0 = new double[] {0.2962962962962963, 0.7037037037037037, 0.0};
                                } else {
                                    var0 = new double[] {0.12244897959183673, 0.8163265306122449, 0.061224489795918366};
                                }
                            } else {
                                if (input[2] <= 6.5) {
                                    var0 = new double[] {0.030303030303030304, 0.9696969696969697, 0.0};
                                } else {
                                    var0 = new double[] {0.0, 0.9, 0.1};
                                }
                            }
                        }
                    }
                } else {
                    if (input[5] <= 0.5) {
                        if (input[7] <= 0.5) {
                            if (input[1] <= 2.5) {
                                if (input[2] <= 8.5) {
                                    var0 = new double[] {0.6739130434782609, 0.32608695652173914, 0.0};
                                } else {
                                    var0 = new double[] {0.03333333333333333, 0.9666666666666667, 0.0};
                                }
                            } else {
                                if (input[2] <= 6.5) {
                                    var0 = new double[] {0.08333333333333333, 0.8888888888888888, 0.027777777777777776};
                                } else {
                                    var0 = new double[] {0.038461538461538464, 0.8461538461538461, 0.11538461538461539};
                                }
                            }
                        } else {
                            if (input[2] <= 4.5) {
                                if (input[6] <= 0.5) {
                                    var0 = new double[] {0.2857142857142857, 0.7142857142857143, 0.0};
                                } else {
                                    var0 = new double[] {0.07692307692307693, 0.9230769230769231, 0.0};
                                }
                            } else {
                                if (input[2] <= 8.5) {
                                    var0 = new double[] {0.0707070707070707, 0.9090909090909091, 0.020202020202020204};
                                } else {
                                    var0 = new double[] {0.03125, 0.8125, 0.15625};
                                }
                            }
                        }
                    } else {
                        if (input[1] <= 2.5) {
                            if (input[2] <= 7.5) {
                                if (input[7] <= 0.5) {
                                    var0 = new double[] {0.0, 1.0, 0.0};
                                } else {
                                    var0 = new double[] {0.0, 0.9117647058823529, 0.08823529411764706};
                                }
                            } else {
                                var0 = new double[] {0.0, 0.5, 0.5};
                            }
                        } else {
                            var0 = new double[] {0.0, 0.3877551020408163, 0.6122448979591837};
                        }
                    }
                }
            }
        } else {
            if (input[1] <= 1.5) {
                if (input[5] <= 0.5) {
                    if (input[4] <= 0.5) {
                        if (input[1] <= 0.5) {
                            if (input[7] <= 0.5) {
                                if (input[2] <= 8.5) {
                                    var0 = new double[] {0.03870967741935484, 0.9612903225806452, 0.0};
                                } else {
                                    var0 = new double[] {0.04, 0.92, 0.04};
                                }
                            } else {
                                if (input[6] <= 0.5) {
                                    var0 = new double[] {0.0, 0.9883720930232558, 0.011627906976744186};
                                } else {
                                    var0 = new double[] {0.0, 0.9629629629629629, 0.037037037037037035};
                                }
                            }
                        } else {
                            if (input[7] <= 0.5) {
                                if (input[2] <= 6.5) {
                                    var0 = new double[] {0.0, 0.9255813953488372, 0.07441860465116279};
                                } else {
                                    var0 = new double[] {0.0, 0.8504672897196262, 0.14953271028037382};
                                }
                            } else {
                                if (input[2] <= 5.5) {
                                    var0 = new double[] {0.0, 0.7288135593220338, 0.2711864406779661};
                                } else {
                                    var0 = new double[] {0.0, 0.5679012345679012, 0.43209876543209874};
                                }
                            }
                        }
                    } else {
                        if (input[1] <= 0.5) {
                            if (input[7] <= 0.5) {
                                var0 = new double[] {0.0, 0.3783783783783784, 0.6216216216216216};
                            } else {
                                var0 = new double[] {0.0, 0.06666666666666667, 0.9333333333333333};
                            }
                        } else {
                            if (input[2] <= 9.5) {
                                var0 = new double[] {0.0, 0.03125, 0.96875};
                            } else {
                                var0 = new double[] {0.0, 0.0, 1.0};
                            }
                        }
                    }
                } else {
                    if (input[1] <= 0.5) {
                        if (input[7] <= 0.5) {
                            if (input[2] <= 5.5) {
                                var0 = new double[] {0.0, 0.8, 0.2};
                            } else {
                                var0 = new double[] {0.0, 0.41379310344827586, 0.5862068965517241};
                            }
                        } else {
                            if (input[2] <= 6.5) {
                                if (input[6] <= 0.5) {
                                    var0 = new double[] {0.0, 0.1794871794871795, 0.8205128205128205};
                                } else {
                                    var0 = new double[] {0.0, 0.2962962962962963, 0.7037037037037037};
                                }
                            } else {
                                var0 = new double[] {0.0, 0.0, 1.0};
                            }
                        }
                    } else {
                        if (input[7] <= 0.5) {
                            if (input[2] <= 6.5) {
                                if (input[2] <= 4.5) {
                                    var0 = new double[] {0.0, 0.09090909090909091, 0.9090909090909091};
                                } else {
                                    var0 = new double[] {0.0, 0.13953488372093023, 0.8604651162790697};
                                }
                            } else {
                                if (input[2] <= 8.5) {
                                    var0 = new double[] {0.0, 0.029411764705882353, 0.9705882352941176};
                                } else {
                                    var0 = new double[] {0.0, 0.0, 1.0};
                                }
                            }
                        } else {
                            var0 = new double[] {0.0, 0.0, 1.0};
                        }
                    }
                }
            } else {
                if (input[7] <= 0.5) {
                    if (input[1] <= 2.5) {
                        if (input[5] <= 0.5) {
                            if (input[2] <= 7.5) {
                                if (input[2] <= 5.5) {
                                    var0 = new double[] {0.0, 0.453125, 0.546875};
                                } else {
                                    var0 = new double[] {0.0, 0.5094339622641509, 0.49056603773584906};
                                }
                            } else {
                                var0 = new double[] {0.0, 0.11904761904761904, 0.8809523809523809};
                            }
                        } else {
                            var0 = new double[] {0.0, 0.0, 1.0};
                        }
                    } else {
                        if (input[2] <= 4.5) {
                            var0 = new double[] {0.0, 0.03125, 0.96875};
                        } else {
                            var0 = new double[] {0.0, 0.0, 1.0};
                        }
                    }
                } else {
                    if (input[1] <= 2.5) {
                        if (input[5] <= 0.5) {
                            if (input[2] <= 4.5) {
                                var0 = new double[] {0.0, 0.1111111111111111, 0.8888888888888888};
                            } else {
                                if (input[6] <= 0.5) {
                                    var0 = new double[] {0.0, 0.02127659574468085, 0.9787234042553191};
                                } else {
                                    var0 = new double[] {0.0, 0.075, 0.925};
                                }
                            }
                        } else {
                            var0 = new double[] {0.0, 0.0, 1.0};
                        }
                    } else {
                        var0 = new double[] {0.0, 0.0, 1.0};
                    }
                }
            }
        }
        return var0;
    }
}