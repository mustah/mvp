import * as classNames from 'classnames';
import * as React from 'react';
import 'Status.scss';
import {statusFor} from '../../../../../types/Types';

interface StatusIconProps {
  code: number;
  content: string;
}

export const StatusIcon = (props: StatusIconProps) => {
  const {code, content} = props;
  const status = statusFor(code);
  if (!status) {
    return null;
  }
  return <div className={classNames('StatusIcon', status)}>{content}</div>;
};
