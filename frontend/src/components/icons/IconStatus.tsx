import {default as classNames} from 'classnames';
import * as React from 'react';
import {EventLogType} from '../../state/domain-models-paginated/meter/meterModels';
import {Status} from '../../types/Types';
import './IconStatus.scss';

interface Props {
  status: Status | EventLogType;
  label: string;
  tooltipText?: string;
}

export const IconStatus = ({status, label, tooltipText}: Props) => (
  <div className={classNames('IconStatus', status)} title={tooltipText}>
    {label}
  </div>
);
