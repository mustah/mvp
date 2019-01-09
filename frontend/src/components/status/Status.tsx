import * as React from 'react';
import {statusTranslation} from '../../helpers/translations';
import {EventLogType} from '../../state/domain-models-paginated/meter/meterModels';
import {Status as StatusType, statusFor} from '../../types/Types';
import {IconStatus} from '../icons/IconStatus';
import {Row} from '../layouts/row/Row';

interface StatusProps {
  label: string;
}

interface Props extends StatusProps {
  type: StatusType | EventLogType;
}

export const Status = ({label}: StatusProps) => (
  <ColoredEvent type={statusFor(label)} label={statusTranslation(label)}/>
);

export const ColoredEvent = ({type, label}: Props) => (
  <Row>
    <IconStatus status={type} label={label}/>
  </Row>
);
