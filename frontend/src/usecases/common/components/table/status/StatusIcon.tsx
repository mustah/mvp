import * as classNames from 'classnames';
import * as React from 'react';
import 'StatusIcon.scss';
import {IdNamed, statusFor} from '../../../../../types/Types';

export const StatusIcon = (props: IdNamed) => {
  const status = statusFor(props.id);
  if (!status) {
    return null;
  }
  return <div className={classNames('StatusIcon', status)}>{props.name}</div>;
};
