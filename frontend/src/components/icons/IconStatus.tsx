import * as classNames from 'classnames';
import * as React from 'react';
import 'IconStatus.scss';
import {IdNamed, statusFor} from '../../types/Types';

export const IconStatus = (props: IdNamed) => {
  const status = statusFor(props.id);
  if (!status) {
    return null;
  }
  return <div className={classNames('IconStatus', status)}>{props.name}</div>;
};
