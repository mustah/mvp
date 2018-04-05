import * as React from 'react';
import {statusTranslation} from '../../helpers/translations';
import {statusFor} from '../../types/Types';
import {IconStatus} from '../icons/IconStatus';
import {Row} from '../layouts/row/Row';

interface Props {
  name: string;
}

export const Status = ({name}: Props) => (
  <Row>
    <IconStatus status={statusFor(name)} name={statusTranslation(name)}/>
  </Row>
);
