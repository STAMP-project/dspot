import sys
import requests

if __name__ == '__main__':

    if len(sys.argv) < 2:
        print 'usage: python upload_to_zenodo <ACCESS_TOKEN> (see https://developers.zenodo.org/#quickstart-upload)'

    ACCESS_TOKEN = sys.argv[1]

    # create the deposition and get the id

    headers = {"Content-Type": "application/json"}
    r = requests.post('https://zenodo.org/api/deposit/depositions',
                      params={'access_token': ACCESS_TOKEN}, json={}, headers=headers)
    if r.status_code != 201:
        print 'something wrong happened during deposition'

    # create the upload

    deposition_id = r.json()['id']
    data = {'filename': 'benchmark.zip'}
    files = {'file': open('benchmark.zip', 'rb')}
    r = requests.post('https://zenodo.org/api/deposit/depositions/%s/files' % deposition_id,
                      params={'access_token': ACCESS_TOKEN}, data=data, files=files)
    if r.status_code != 201:
        print 'something wrong happened during upload\'s creation'

    # add some metadatas

    data = {
        'metadata': {
            'title': 'benchmark_dspot_prettifier',
                'upload_type': 'zip',
                'description': 'The zip of the benchmarch for DSpot-prettifier',
                'creators': [{'name': 'Danglot, Benjamin',
                'affiliation': 'Inria'}]
        }
    }

    r = requests.put('https://zenodo.org/api/deposit/depositions/%s' % deposition_id,
                     params={'access_token': ACCESS_TOKEN}, data=json.dumps(data), headers=headers)
    if r.status_code != 200:
        print 'something wrong happened during metadata\'s update'

    # upload

    r = requests.post('https://zenodo.org/api/deposit/depositions/%s/actions/publish' % deposition_id,
                      params={'access_token': ACCESS_TOKEN} )
    if r.status_code != 202:
        print 'something wrong happened during upload'

