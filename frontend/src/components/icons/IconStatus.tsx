import * as classNames from 'classnames';
import * as React from 'react';
import {Status} from '../../types/Types';
import './IconStatus.scss';

interface Props {
  status: Status;
  label: string;
  tooltipText?: string;
}

export const IconStatus = ({status, label, tooltipText}: Props) => (
  <div className={classNames('IconStatus', status)} title={tooltipText}>
    {label}
  </div>
);
