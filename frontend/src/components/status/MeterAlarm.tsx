import * as React from 'react';
import {translate} from '../../services/translationService';
import {Alarm} from '../../state/domain-models-paginated/meter/meterModels';
import {Status} from '../../types/Types';
import {IconStatus} from '../icons/IconStatus';
import {Row} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';

interface Props {
  status: Status;
  label: string;
  tooltipText?: string;
}

interface MeterAlarmProps {
  alarms?: Alarm[];
}

const AlarmComponent = (props: Props) => (
  <Row>
    <IconStatus {...props}/>
  </Row>
);

export const MeterAlarm = ({alarms}: MeterAlarmProps) => (
  alarms && alarms.length
    ? <AlarmComponent label={translate('yes')} status={Status.error} />
    : <Normal>-</Normal>
);
