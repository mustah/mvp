import * as React from 'react';
import {translate} from '../../services/translationService';
import {Alarm} from '../../state/domain-models-paginated/meter/meterModels';
import {Status} from '../../types/Types';
import {IconStatus} from '../icons/IconStatus';
import {Row} from '../layouts/row/Row';

interface Props {
  status: Status;
  label: string;
  tooltipText?: string;
}

interface MeterAlarmProps {
  alarm?: Alarm;
}

const AlarmComponent = (props: Props) => (
  <Row>
    <IconStatus {...props}/>
  </Row>
);

export const MeterAlarm = ({alarm}: MeterAlarmProps) => (
  alarm
    ? <AlarmComponent label={`${alarm.mask}`} status={Status.error} tooltipText={alarm.description}/>
    : <AlarmComponent label={translate('ok')} status={Status.ok}/>
);
