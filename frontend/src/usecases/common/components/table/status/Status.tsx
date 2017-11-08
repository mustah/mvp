import * as React from 'react';
import {statusFor} from '../../../../../types/Types';
import './Status.scss';
import {StatusIcon} from './StatusIcon';
import {Row} from '../../layouts/row/Row';

interface StatusProps {
  code: number;
  content: string;
}

export const Status = (props: StatusProps) => {
  const {code, content} = props;
  if (!statusFor(code)) {
    return null;
  }
  return (
    <Row className="Status">
      <StatusIcon code={code} content={content}/>
    </Row>
  );
};
