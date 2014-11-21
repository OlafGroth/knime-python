package org.knime.code.generic;

public class SourceCodeTemplate implements Comparable<SourceCodeTemplate> {

	private String m_fileName;
	private String m_category;
	private String m_title;
	private String m_description;
	private String m_sourceCode;

	public SourceCodeTemplate(final String fileName, final String category,
			final String title, final String description,
			final String sourceCode) {
		m_fileName = fileName;
		m_category = category;
		m_title = title;
		m_description = description;
		m_sourceCode = sourceCode;
	}

	public String getFileName() {
		return m_fileName;
	}

	public String getCategory() {
		return m_category;
	}

	public String getTitle() {
		return m_title;
	}

	public String getDescription() {
		return m_description;
	}

	public String getSourceCode() {
		return m_sourceCode;
	}

	@Override
	public String toString() {
		return m_title;
	}

	@Override
	public int compareTo(SourceCodeTemplate o) {
		int result = getTitle().compareTo(o.getTitle());
		if (result == 0) {
			result = getFileName().compareTo(o.getFileName());
		}
		return result;
	}

}