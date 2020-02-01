
# Development

## Directories

Projects being analysed by the DSpot Web UI are cloned and analysed in a dedicated directory, configured in section `work_dir` of the main configuration file of the application: `d_spot_web.conf`.

In this directory each directory is a project, with all required files and information for the project stored inside. A typical architecture looks as follows:

* `work_dir`
  - `project_a`
    - `src` is the git extract (clone) of the repository.
    - `output` is the hierarchy generated during the dspot execution.
    - `results.zip` is the compressed file of the results.
    - `logs` contains the logs of all major actions:
      - `dspot.log` is the log of the dspot execution.
      - `git_clone.log` is the log of the `git clone` command.
      - `git_pull.log` is the log of the last `git pull` command, if relevant.




qsfd
