import {default as classNames} from 'classnames';
import ActionSearch from 'material-ui/svg-icons/action/search';
import ContentClear from 'material-ui/svg-icons/content/clear';
import * as React from 'react';
import {colors} from '../../app/colors';
import {svgIconProps} from '../../app/themes';
import {ClassNamed, OnChange} from '../../types/Types';
import './SearchBox.scss';
import SvgIconProps = __MaterialUI.SvgIconProps;

interface Props extends ClassNamed {
  onChange: OnChange;
  onClear: OnChange;
  clear?: boolean;
  value?: string;
}

interface State {
  value: string;
}

const style: React.CSSProperties = {
  cursor: 'pointer',
  position: 'absolute',
  right: 12,
  top: 6,
  color: colors.lightBlack,
};

const styles: SvgIconProps = {
  style,
  ...svgIconProps
};

export class SearchBox extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {value: props.value || ''};
  }

  componentWillReceiveProps({value}: Props) {
    if (this.props.value && value === undefined) {
      this.setState({value: ''});
    }
  }

  render() {
    const {className, clear} = this.props;
    const {value} = this.state;

    return (
      <div className={classNames('SearchBox', className)}>
        <input
          autoFocus={true}
          type="textfield"
          className="SearchBox-input"
          value={clear ? '' : value}
          onChange={this.onChange}
        />
        {value ? <ContentClear onClick={this.onClear} {...styles}/> : <ActionSearch style={style}/>}
      </div>
    );
  }

  onChange = (event: any) => {
    event.preventDefault();
    const value = event.target.value;
    this.setState({value});
    this.props.onChange(value);
  }

  onClear = () => {
    const value = '';
    this.setState({value});
    this.props.onClear(value);
  }
}
