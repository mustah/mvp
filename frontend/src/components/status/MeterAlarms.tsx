import * as React from 'react';
import {Info} from '../../containers/dialogs/Info';
import {formatAlarmMaskHex} from '../../helpers/formatters';
import {translate} from '../../services/translationService';
import {Alarm} from '../../state/domain-models-paginated/meter/meterModels';
import {Status} from '../../types/Types';
import {IconStatus} from '../icons/IconStatus';
import {Row} from '../layouts/row/Row';
import {Bold} from '../texts/Texts';
import './MeterAlarms.scss';

interface Props {
  status: Status;
  label: string;
  className?: string;
  tooltipText?: string;
}

interface MeterAlarmProps {
  items?: Alarm[];
}

interface MeteringStatusProps {
  isReported: boolean;
  label?: string;
}

interface AlarmStatusProps {
  hasAlarm: boolean;
}

const StatusComponent = ({status, label, tooltipText, className}: Props) => (
  <Row className={className}>
    <IconStatus status={status} label={label} tooltipText={tooltipText}/>
  </Row>
);

export const MeterAlarms = ({items}: MeterAlarmProps) => {
  const alarmStatuses = Array.isArray(items) && items.length > 0
    ? items.map(({id, description, mask}, index) =>
      (
        <StatusComponent
          key={`alarms-${id}-${index}`}
          className="MeterAlarms-description"
          label={description || (translate('unknown alarm') + ` (${translate('alarm code')} ${mask})`)}
          status={Status.error}
          tooltipText={`${translate('alarm code')}: ${formatAlarmMaskHex(mask)}`}
        />
      )
    ) : undefined;

  return (
    <Info label={translate('alarm')}>
      {alarmStatuses || <Bold>-</Bold>}
    </Info>
  );
};

export const AlarmStatus = ({hasAlarm}: AlarmStatusProps) =>
  hasAlarm
    ? <StatusComponent label={translate('yes')} status={Status.error}/>
    : <Bold>-</Bold>;

export const MeteringStatus = ({isReported, label}: MeteringStatusProps) =>
  isReported
    ? <StatusComponent label={label || translate('yes')} status={Status.error}/>
    : <Bold>-</Bold>;
