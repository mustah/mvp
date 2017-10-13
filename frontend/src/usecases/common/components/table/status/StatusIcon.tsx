import * as classNames from 'classnames';
import * as React from 'react';
import {statusFor} from '../../../../../types/Types';

interface StatusIconProps {
  code: number;
}

export const StatusIcon = (props: StatusIconProps) => {
  const {code} = props;
  const status = statusFor(code);
  if (!status) {
    return null;
  }
  return <div className={classNames('StatusIcon', status)}/>;
};
