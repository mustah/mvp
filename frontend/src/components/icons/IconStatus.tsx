import * as classNames from 'classnames';
import * as React from 'react';
import {IdNamed, statusFor} from '../../types/Types';
import './IconStatus.scss';

export const IconStatus = (props: IdNamed) => {
  const status = statusFor(props.id);
  if (!status) {
    return null;
  }
  return <div className={classNames('IconStatus', status)}>{props.name}</div>;
};
