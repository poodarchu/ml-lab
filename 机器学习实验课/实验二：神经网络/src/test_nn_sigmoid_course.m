function test_nn_sigmoid_course
% close all;
x = load('data/wdbc.txt');  y = load('data/wdbclabel.txt');
x = mapminmax(x', 0, 1)';   y = y - min(y);
% ?????
[param,model]=initParam(x);
% ??
model=trainNN(x,y,model,param);
% ??
[acc,output,label]=predictNN(x,y,model,param);
% ????
subplot(2,2,1); 
tt=1:1:size(model.J,2);
plot(tt,model.J,'r-',tt,model.J1,'b-',tt,model.J2,'g-'); legend('J','J1','J2'); title('target-value J');
subplot(2,2,2); plot(output);   title('output');
subplot(2,2,3); plot(label);    title('output-label');
subplot(2,2,4); plot(y);        title('fact-y');
acc
save workspace;
end

%% ?????
function [param,model]=initParam(x)
param.l1 = size(x,2);                               % ?????????????
param.l2 = 5;                                       % ???????
param.m = size(x,1);                                % ????
param.lamda = 0.05;                                 % ????
param.beta = 0.01;                                  % ????
param.beta1 = 0.01;                                % ????
% param.beta2 = 0.01;                                 % ????
param.e = 0.001;                                     % ??????
param.itermax = 500;                                % ??????
tim = 10; diff=tim/2;                               % ????????
model.w1 = rand(param.l2,param.l1)*tim-diff;        % ??????
model.w2 = rand(param.l2,1)*tim-diff;               % ??????
model.b1 = rand(param.l2,1)*tim-diff;               % ????+1??
model.b2 = rand(1,1)*tim-diff;                      % ????+1??
end

%% ??
function model=trainNN(x,y,model,param)
iter = 1; err = param.e+1; 
w1 = model.w1; w2 = model.w2; b1 = model.b1; b2 = model.b2; 
[J(1),J1(1),J2(1)] = compJ(w1,w2,b1,b2,x,y,param);
while iter < param.itermax && abs(err) >= param.e
    Q = compQk(w1,w2,b1,b2,x,y,param);
    %% update w2
    for k=1:param.m
        J_w2 = 2.0/param.m*sum(Q(k)*(w1*x(k,:)'+b1)) + 2 * param.lamda * w2
    end
    w2 = w2 - param.beta1*J_w2   
    
    %% update b2
    for k=1:param.m
        J_b2 = 2.0/param.m*sum(Q(k))
    end
    b2 = model.b2 - param.beta1*J_b2
    
    
    %% update w1
    for k=1:param.m
        J_w1 = 2.0/param.m*sum(Q(k)*(w1*x(k,:)')) + 2*param.lamda*w1
    end
    w1 = w1-param.beta*J_w1
    
    %% update b1
    for k=1:param.m
        J_b1 = 2.0/param.m*sum(Q(k)*w2)
    end
    b1 = model.b1 - param.beta*J_b1
    
    %% cal err
    [J(iter+1), J1(iter+1), J2(iter+1)]= compJ(w1,w2,b1,b2,x,y,param);
    err = J(iter+1) - J(iter);
    fprintf('err=%f, J=%f, b2=%f\n',err,J(iter),b2);
    iter = iter+1;
end
model.J = J; model.J1 = J1; model.J2 = J2;
model.w1 = w1; model.w2=w2; model.b1=b1; model.b2=b2;
end

%% ??Q
function Q = compQk(w1,w2,b1,b2,x,y,param)
for k=1:param.m
    hxk = sum(w2.*(w1*x(k,:)'+b1))+ b2;
    Q1(k) = 1/(1+exp(-hxk)) - y(k);
    F(k) = exp(-hxk)/(1+exp(-hxk))^2;
    Q(k)=Q1(k)*F(k);
end
end

%% ??J
function [J,J1,J2] = compJ(w1,w2,b1,b2,x,y,param)
J1 = 0;
for k=1:param.m
    hxk=hx(x(k,:),w1,w2,b1,b2,param);
    fxk=1/(1+exp(-hxk));
    J1 = J1 + (fxk-y(k))^2;
end
J2 = param.lamda * (sum(sum(w1.^2)) + sum(w2.^2));
J = J1 + J2;
end

%% ??hx(k)
function hxk=hx(xk,w1,w2,b1,b2,param)
hxk=0;
for j=1:param.l2
    hxk=hxk+w2(j)*(w1(j,:)*xk'+b1(j))+b2;
end
end

%% ??
function [acc,output,label]=predictNN(x,y,model,param)
acc=0;
for k=1:param.m
    z(k)=hx(x(k,:),model.w1,model.w2,model.b1,model.b2,param);
    fxk=1/(1+exp(-z(k)));
    output(k,1)=fxk;
end
label = output>0.5;
acc = sum(label==y)/param.m;
end


