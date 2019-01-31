
import sys
import json
import subprocess

is_number_2 = False

numbers = [
    '20000',
    '15000',
    '12500',
    '10000',
    '9000',
    '8000',
    '7000',
    '6500',
    '6000',
    '5500',
    '5000',
    '4750',
    '4500',
    '4250',
    '4000',
    '3750',
    '3500',
    '3400',
    '3300',
    '3200',
    '3100',
    '3000',
    '2900',
    '2800',
    '2700',
    '2600',
    '2500',
    '2400',
    '2300',
    '2200'
]

numbers_2 = [
    '2200',
    '2100',
    '2000',
    '1950',
    '1900',
    '1850',
    '1800',
    '1750',
    '1700',
    '1650',
    '1600',
    '1550'
]

cmd = 'curl -o java_repositories_{}.json https://api.github.com/search/repositories\?q\=stars:{}+language:java -H "Authorization: token "'


def curl(parameter, prefix):
    final_cmd = cmd.format(parameter, prefix + parameter.replace('_', '..'))
    print final_cmd
    subprocess.call(final_cmd, shell=True)


def execute(parameter, prefix=''):
    output = []
    curl(parameter, prefix)
    with open('java_repositories_{}.json'.format(parameter)) as json_data:
        data_dict = json.load(json_data)
    for project in data_dict['items']:
        print project['full_name'], project['stargazers_count']
        output.append(project['full_name'])
    print len(output)
    return output

def run():
    global numbers
    global numbers_2
    global is_number_2
    output = []
    if is_number_2:
        numbers = numbers_2
    else:
        output = execute(numbers[0], prefix='\>\=')
    for i in range(0, len(numbers) - 2):
        tmp = execute(''.join([numbers[i + 1], '_', numbers[i]]))
        for t in tmp:
            output.append(t)
    for t in tmp:
        output.append(t)
    mode = 'a' if is_number_2 else 'w'
    with open('projects.txt', mode) as file_out:
        file_out.write('\n'.join(output))

if __name__ == '__main__':
    is_number_2 = len(sys.argv) > 1 and sys.argv[1] == '2'
    run()