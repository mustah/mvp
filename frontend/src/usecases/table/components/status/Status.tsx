import * as React from 'react';
import {states} from '../../../../types/Types';
import {Row} from '../../../layouts/components/row/Row';
import './Status.scss';
import {StatusIcon} from './StatusIcon';

interface StatusProps {
  code: number;
  content?: string;
}

export const Status = (props: StatusProps) => {
  const {code, content} = props;
  const status = states(code);
  if (!status.valid) {
    return null;
  }
  return (
    <Row className="Status">
      <StatusIcon code={code}/>
      <div className="Status-text">
        {content}
      </div>
    </Row>
  );
};
