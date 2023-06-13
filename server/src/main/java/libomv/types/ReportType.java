package libomv.types;


public enum ReportType
{
	// No report
	None,
	// Unknown report type
	Unknown,
	// Bug report
	Bug,
	// Complaint report
	Complaint,
	// Customer service report
	CustomerServiceRequest;

	public static ReportType setValue(final int value)
	{
		if (0 <= value && value < ReportType.values().length)
			return ReportType.values()[value];
		return null;
	}

	public byte getValue()
	{
		return (byte) this.ordinal();
	}
}
