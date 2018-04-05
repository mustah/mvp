import * as classNames from 'classnames';
import * as React from 'react';
import {Status} from '../../types/Types';
import './IconStatus.scss';

interface Props {
  status: Status;
  name: string;
}

export const IconStatus = ({status, name}: Props) => (
  <div className={classNames('IconStatus', status)}>
    {name}
  </div>
);
