import * as classNames from 'classnames';
import * as React from 'react';
import {states} from '../../../../types/Types';

interface StatusIconProps {
  code: number;
}

export const StatusIcon = (props: StatusIconProps) => {
  const {code} = props;
  const status = states(code);
  if (!status.valid) {
    return null;
  }
  return <div className={classNames('StatusIcon', status.state)}/>;
};
