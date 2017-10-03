import * as classNames from 'classnames';
import * as React from 'react';
import {States} from '../../../../types/Types';
import './StatusIcon.scss';

interface StatusIconProps {
  statusCode: number;
}

export const StatusIcon = (props: StatusIconProps) => {
  const {statusCode} = props;
  const status = States(statusCode);
  if (!status.valid) {
    return null;
  }
  return (
    <div className={classNames('StatusIcon', status.state)}/>
  );
};
