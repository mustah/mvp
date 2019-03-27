import {isPlainObject} from 'lodash';
import * as React from 'react';
import {translate} from '../../services/translationService';
import {Alarm} from '../../state/domain-models-paginated/meter/meterModels';
import {Status} from '../../types/Types';
import {IconStatus} from '../icons/IconStatus';
import {Row} from '../layouts/row/Row';
import {BoldFirstUpper} from '../texts/Texts';

interface Props {
  status: Status;
  label: string;
  tooltipText?: string;
}

interface MeterAlarmProps {
  items?: Alarm[] | Alarm | boolean;
  label?: string;
}

const AlarmComponent = (props: Props) => (
  <Row>
    <IconStatus {...props}/>
  </Row>
);

export const MeterAlarm = ({items, label}: MeterAlarmProps) => (
  Array.isArray(items) && items.length > 0 || items === true || isPlainObject(items)
    ? <AlarmComponent label={label || translate('yes')} status={Status.error}/>
    : <BoldFirstUpper>-</BoldFirstUpper>
);
