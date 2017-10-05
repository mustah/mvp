import * as classNames from 'classnames';
import * as React from 'react';
import {States} from '../../../../types/Types';
import './StatusIcon.scss';

interface StatusIconProps {
  code: number;
  content?: string;
}

export const StatusIcon = (props: StatusIconProps) => {
  const {code, content} = props;
  const status = States(code);
  if (!status.valid) {
    return null;
  }
  return (
    <div>
      <div className={classNames('StatusIcon', status.state)}/>
      {content}
    </div>
  );
};
