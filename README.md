# Task-a-Log

A simple system to log (lack of) completion of complex tasks in a software system.

## The problem

In a microservices architecture, some components emit a message that should cause the execution of certain tasks in
other components. This may happen directly, using an API call, others may be conveyd by asynchronous systems, such as
message queues.

The completion of a task composed of multiple sub-tasks distributed on different microservices can be hard, whether
each subtask can be executed in parallel or should execute in a particular order.

**Task-a-Log** purpose is to centralize the logging of complex tasks, or, more specifically, log and quickly notify
when a task has not completed in time.

## Configuration

### The tasks

A task is a complex process that is made of multiple steps.
Tasks are defined in the `etc/tasks` directory.

A task is made of:

* `id`: the ID of the task. **note**: the filename should be `[id].yml`

* `max_duration_seconds`: the maximum number of seconds since the start of the task before the whole task is considered
not on schedule, or too late

* `ordered`:  true if the sub-tasks should execute in order to be valid

* `steps`: the steps (or sub-tasks) comprising the task

Each task is made of:

* `id`: the ID of the step

* `status`: the starting status of the step

* `expected_status`: the status that is expected to consider the step successfully executed


### The timers

A timer is an event that is expected to happen within a certain time period.
Timers are defined in the `etc/timers` directory.

A timer starts when the first request is performed to it. 

A timer is made of:

* `id`: the ID of the timer. **note**: the filename should be `[id].yml`

* `within_seconds`: the period length in seconds

### The notifiers

You may want to activate notifications to trigger when a condition is not met.

To activate a notificator, edit the `notificator` list in the `etc/application.yml` file.

Currently we support a rudimentary email notificator. E-mail settings can be confiured in the `mail_notification.yml`
file.
 
 
### Results

Results are logged in the `results` directory.

## Interactions

Currently the only communication protocol available to communicate an event to Task-A-Log is HTTP calls to an API.

* `POST /worklog/submit`: to submit an task event. The body is made of:
    
    * `id`: the ID of the **transaction** which needs to be shared by all submissions generated by the same event
    
    * `task_id`: the task ID
    
    * `step_id`: the ID of the step
    
    * `sender_id`: the ID of the sender
    
    * `status`: the status of the step
    
    Example:
    
    ```json
    {
    	"id": "17185",
    	"task_id": "Scheduler",
    	"step_id": "executed",
    	"sender_id": "AFScheduler",
    	"status": "DONE"
    }
    ```
* `POST /timer/submit`: to submit a timer event. The body is made of:
    
    * `id`: The ID of the timer
    
    Example:
    
    ```json
    {
    	"id":"EmailTimer"
    }
    ```

## Integration

Let's analyze how to use Task-A-Log in a real world scenario.

### The Scheduled task example 

A microservice will trigger a message based on a schedule. We don't care how the message is delivered to the rest of the
system.

The second step is the message must be delivered and accepted by another microservice.

The third step is a certain operation is executed due to the content of that message.

We want to validate that the tasks complete all the time.

First of all, **all the tasks** need to have a unique ID that will follow the execution of the task throughout the whole
process.

The task will be defined as follows:

```yaml
task_id: Scheduler
max_duration_seconds: 10
ordered: true
steps:
  - id: emit
    status: NONE
    expected_status: DONE
  - id: delivered
    expected_status: DONE
    status: NONE
  - id: executed
    expected_status: DONE
    status: NONE
```

Once one process is started, the Scheduler will submit the following message to Task-A-Log:

```json
{
	"id": "17185",
	"task_id": "Scheduler",
	"step_id": "emit",
	"sender_id": "AFScheduler",
	"status": "DONE"
}
```

Then when the message is accepted by an Executor, the following will be submitted by the receiving microservice:

```json
{
	"id": "17185",
	"task_id": "Scheduler",
	"step_id": "delivered",
	"sender_id": "AFScheduler",
	"status": "DONE"
}
```

Finally, when the Executor is done, it will submit the following:
```json
{
	"id": "17185",
	"task_id": "Scheduler",
	"step_id": "executed",
	"sender_id": "AFScheduler",
	"status": "DONE"
}
```

If the entire process completes **within 10 seconds** and in this **exact order**, the task is considered a success


Now, assume that the scheduler might stop triggering for some internal reason, without really shutting down. What we
know, though, is one no less than 1 message should be emitted every hour. We want to verify this happens consistently,
by using a timer.

Let's define a timer like this:

```yaml
id: EmailTimer
within_seconds: 3600
```

The Scheduler is in charge of submitting the following data to the timer every time a message is emitted:

```json
{
	"id":"EmailTimer"
}
```

If, in the time interval, **at least one message** is sent, then the timer is valid.
