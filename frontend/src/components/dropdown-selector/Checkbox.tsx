import * as classNames from 'classnames';
import * as React from 'react';
import {Children, ClassNamed, Clickable, uuid} from '../../types/Types';
import {RowMiddle} from '../layouts/row/Row';
import './Checkbox.scss';

interface Props extends Clickable, ClassNamed {
  id: uuid;
  checked?: boolean;
  style: React.CSSProperties;
  label: Children;
}

interface State {
  checked?: boolean;
}

export class Checkbox extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {checked: props.checked};
  }

  render() {
    const {label, style, className} = this.props;
    const {checked} = this.state;
    return (
      <RowMiddle className="Checkbox" style={style}>
        <label className={classNames('clickable', className, {Bold: checked})}>
          <input
            type="checkbox"
            onClick={this.onClick}
            defaultChecked={checked}
          />
          {label}
        </label>
      </RowMiddle>
    );
  }

  onClick = () => {
    this.setState((prevState: State) => ({checked: !prevState.checked}));
    this.props.onClick();
  }
}
